import com.google.gson.*;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * This class represents a connection to an urbit ship. It can be used to send messages to a respective ship over the eyre protocol.
 */
public class Urbit {

	/**
	 * The code is the deterministic password used to authenticate with an Urbit ship
	 * It can be obtained by running `+code` in the dojo.
	 */
	private final String code;

	/**
	 * The URL representing the location where eyre is listening for requests on the ship
	 */
	private final String url;


	/**
	 * Used to generate a unique channel name. A channel name is typically the current unix time plus a random hex string
	 */
	private String uid;

	/**
	 * The ID of the last request we sent to the server
	 */
	private int requestId = 0;

	/**
	 * he id of the last event received from the ship
	 */
	private int lastSeenEventId = 0;

	/**
	 * The id of the last event we acked to the server
	 */
	private int lastAcknowledgedEventId = 0;


	/**
	 * The SSE Client responsible for receiving events from the ship.  Starts off as null and is initialized later; we don't want to start polling until it the channel exists
	 */
	private EventSource sseClient;

	/**
	 * The authentication cookie given to us after logging in with the {@link Urbit#code}.
	 * Note: it is possible to authenticate with an incorrect +code and still get an auth cookie.
	 * Only after sending the first real request will it fail.
	 */
	private Cookie cookie;


	/**
	 * The authentication status of the ship
	 * <p>
	 * N.B: This does not imply a "successful" authentication, because eyre gives you an auth cookie whether you use the correct password or not.
	 * Only when you actually go to make a request will it fail with a 401 or something of the like.
	 *
	 * </p>
	 *
	 * @return whether or not we have authenticated with the ship.
	 */
	public boolean isAuthenticated() {
		return this.cookie != null;
	}

	/**
	 * Get the connection status of the ship
	 *
	 * @return whether or not the ship is connected
	 */
	public boolean isConnected() {
		return this.isAuthenticated() && this.sseClient != null;
	}


	/**
	 * The name of the ship that we are connecting to. (the @p without '~')
	 */
	private final String shipName;

	/**
	 * @return the name of the ship that we are connecting to
	 */
	public String getShipName() {
		return shipName;
	}

	/**
	 * This is a Map between event-id of a poke request and the respective handler function.
	 * When the sseClient receives an {@link EyreResponse}, it propagates the data in the form of a {@link PokeResponse}
	 * to the correct handler function.
	 */
	private final Map<Integer, CompletableFuture<PokeResponse>> pokeHandlers;

	/**
	 * This is the equivalent mapping for subscription handlers. See {@link Urbit#pokeHandlers}.
	 */
	private final Map<Integer, Consumer<SubscribeEvent>> subscribeHandlers;

	public static final MediaType JSON
			= MediaType.get("application/json; charset=utf-8");

	private final OkHttpClient client;

	private final Gson gson;

	/**
	 * Synchronization object used to prevent multithreading errors and incorrectly ordered network calls
	 */
	private final Object urbitLock = new Object();


	/**
	 * Constructs a new Urbit connection.
	 *
	 * <p>
	 * Please note that the connection times out after 1 day of not having received any events from a ship
	 * </p>
	 *
	 * @param url      The URL (with protocol and port) of the ship to be accessed
	 * @param shipName The name of the ship to connect to (@p)
	 * @param code     The access code for the ship at that address
	 */
	public Urbit(String url, String shipName, String code) {
		this.uid = Math.round(Math.floor(Instant.now().toEpochMilli())) + "-" + Urbit.hexString(6);
		this.code = code;
		this.url = url;
		this.pokeHandlers = new HashMap<>();
		this.subscribeHandlers = new HashMap<>();
		this.shipName = requireNonNull(shipName);
		this.cookie = null;

		// init cookie manager to use `InMemoryCookieStore` by providing null
		CookieHandler cookieHandler = new CookieManager(null, CookiePolicy.ACCEPT_ALL);


		this.client = new OkHttpClient.Builder()
				.cookieJar(new JavaNetCookieJar(cookieHandler))
				.readTimeout(1, TimeUnit.DAYS)  // possible max length of session (time before we get an event back) (as per https://stackoverflow.com/a/47232731) // todo possibly adjust timeout duration might be too aggressive
				.build();

		gson = new Gson();

		// todo, see if we want to punt up the IOException to the user or just consume it within the API or even make a custom exception (may be overkill).
		// todo make nice parsing data classes for known apps (i.e. a ChatUpdatePayload class for chat-view subscription)
		//  cause there is no clean way to access nested values with a raw gson object

		// todo what about different marks. so far I've only ever encountered helm-hi or json, but the api only really accepts `JsonElement`s
	}

	/**
	 * Returns the next event ID for the appropriate channel.
	 */
	private int nextID() {
		this.requestId++;
		return this.requestId;
	}

	/**
	 * This is basic interpolation to get the channel URL of an instantiated Urbit connection.
	 */
	public String getChannelUrl() {
		return this.url + "/~/channel/" + this.uid;
	}

	@NotNull
	public String getLoginUrl() {
		return this.url + "/~/login";
	}

	public String getScryUrl(String app, String path, String mark) {
		return this.url + "/~/scry" + app + "/" + path + "." + mark;
	}

	public String getSpiderUrl(String inputMark, String threadName, String outputMark) {
		return this.url + "/spider/" + inputMark + "/" + threadName + "/" + outputMark + ".json";
	}

	/**
	 * Connects to the Urbit ship. Nothing can be done until this is called.
	 * @return Returns an immutable wrapper around a response body object
	 */
	public InMemoryResponseWrapper authenticate() throws IOException {
		RequestBody formBody = new FormBody.Builder()
				.add("password", this.code)
				.build();

		Request request = new Request.Builder()
//				.header("connection", "keep-alive")
				.url(this.getLoginUrl())
				.post(formBody)
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) throw new IOException("Error: " + response);
		// todo figure out best way to return an immutable response body obj or something
		//  or design api around it or use different library.
		// basically, response.body() is a one-shot obj that needs to be copied manually so it sucks
		// we can't call it multiple times


		// after we made the request, here we extract the cookie. its quite ceremonial
		this.cookie = this.client.cookieJar().loadForRequest(requireNonNull(HttpUrl.parse(this.getChannelUrl())))
				.stream()
				.filter(cookie1 -> cookie1.name().startsWith("urbauth"))
				.findFirst().orElseThrow(() -> new IllegalStateException("Did not receive valid authcookie"));
		// stream api is probably expensive and extra af but this is basically necessary to prevent brittle behavior

		return new InMemoryResponseWrapper(response);
	}


	/**
	 * Creates the channel on which the sseClient will be instantiated on
	 * This must be done in the same breath as creating the sseClient (i.e. in {@link Urbit#connect()},
	 * otherwise we will never be able to create a connection to the ship.
	 *
	 * @throws IOException when the poke network request fails
	 */
	private void createChannel() throws IOException {
		JsonPrimitive jsonPayload = new JsonPrimitive("Opening Airlock :)");
		this.poke(this.getShipName(), "hood", "helm-hi", jsonPayload);
	}

	/**
	 * Initializes the SSE pipe for the appropriate channel (if necessary). Must be called after authenticating
	 */
	public void connect() throws IOException {
		if (this.sseClient != null) {
			return;
		}
		if (!this.isAuthenticated()) {
			throw new IllegalStateException("Cannot connect to ship without being authenticated");
		}

		this.createChannel(); // We MUST create the channel before sending the sseRequest. Only after doing both will we get a response from the ship.
		// That is, we cannot wait for a poke response back because before creating the sseClient because we'll never have established one in the first place

		Request sseRequest = new Request.Builder()
				.url(this.getChannelUrl())
				.header("connection", "keep-alive")
				.build();


		this.sseClient = EventSources.createFactory(this.client)
				.newEventSource(sseRequest, new EventSourceListener() {
					@Override
					public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
						int eventID = Integer.parseInt(requireNonNull(id, "Got null id")); // this thing is kinda useless

						synchronized (urbitLock) {
							EyreResponse eyreResponse = gson.fromJson(data, EyreResponse.class);
							lastSeenEventId = eyreResponse.id;

							System.out.println(",=============Event==============,");
							System.out.println("raw: " + data);
							System.out.println("event id from okhttp " + eventID);
							System.out.println("lastSeenEventId: " + lastSeenEventId);
							System.out.println("lastAckedEventId: " + lastAcknowledgedEventId);
							System.out.println("got eyre response data");
							System.out.println(eyreResponse);
							System.out.println(".=============Event==============.");

							try {
								if (lastSeenEventId != lastAcknowledgedEventId) {
									ack(lastSeenEventId);
									lastAcknowledgedEventId = lastSeenEventId;
								}
							} catch (IOException e) {
								throw new IllegalStateException("could not ack");
							}



							//if (eyreResponse.id != eventD) {
							//throw new IllegalStateException("invalid ids or something");
							//}

							switch (eyreResponse.response) {
								case "poke":
									var pokeHandler = pokeHandlers.get(eyreResponse.id);
									if (eyreResponse.isOk()) {
										pokeHandler.complete(PokeResponse.SUCCESS);
									} else {
										pokeHandler.complete(PokeResponse.fromFailure(eyreResponse.err));
									}
									pokeHandlers.remove(eyreResponse.id);
									break;
								case "subscribe":
									var subscribeHandler = subscribeHandlers.get(eyreResponse.id);
									if (eyreResponse.isOk()) {
										subscribeHandler.accept(SubscribeEvent.STARTED);
									} else {
										subscribeHandler.accept(SubscribeEvent.fromFailure(eyreResponse.err));
										subscribeHandlers.remove(eyreResponse.id);
									}
									break;
								case "diff":
									subscribeHandler = subscribeHandlers.get(eyreResponse.id);
									subscribeHandler.accept(SubscribeEvent.fromUpdate(eyreResponse.json));
									break;
								case "quit":
									subscribeHandler = subscribeHandlers.get(eyreResponse.id);
									subscribeHandler.accept(SubscribeEvent.FINISHED);
									subscribeHandlers.remove(eyreResponse.id);
									break;

								default:
									throw new IllegalStateException("Got unknown eyre responseType");
							}
						}
					}

					@Override
					public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
						if (t != null) {
							System.err.println("Encountered error while doing sse stuff");
							throw new RuntimeException(t);
						}
						if (response != null && response.code() != 200) {
							System.err.println("Event Source Error: " + response);
							return;
						}
						// todo figure out what to do here
						System.out.println("Got 200 OK on " + requireNonNull(response).request().url());
					}

					@Override
					public void onClosed(@NotNull EventSource eventSource) {
						// todo see if more adaptation is needed
						//  as per https://github.com/dclelland/UrsusAirlock/blob/master/Ursus%20Airlock/Airlock.swift#L196

						// todo possibly extract this code out to main class
						System.out.println("!!!!!!!!!!Closing!!!!!!!!!!!!");
						sseClient = null;
						uid = Urbit.uid();
						requestId = 0;
						lastSeenEventId = 0;
						lastAcknowledgedEventId = 0;
						pokeHandlers.clear();
						subscribeHandlers.clear();
					}
				});
	}


	/**
	 * This is a wrapper method that can be used to send any action with data.
	 * <p>
	 * Every message sent has some common parameters, like method, headers, and data
	 * structure, so this method exists to prevent duplication.
	 * </p>
	 *
	 * @param jsonData The data to send with the action
	 * @return
	 */
	public InMemoryResponseWrapper sendJSONtoChannel(JsonObject jsonData) throws IOException {
		synchronized (urbitLock) {
			JsonArray fullJsonDataArray = new JsonArray();
			JsonObject fullJsonData = jsonData.deepCopy(); // todo seems like a wasteful way to do it, if outside callers are using this method; possibly refactor
			//  if we make this method private then we can avoid this because we are the only ones ever calling the method so we can basically just make sure that we never call it with anything that we use later on that would be affected by the mutability of the json object
			fullJsonDataArray.add(fullJsonData);

			// todo is this correct behavior??
			// commenting it out seems to not have an effect, i.e. tests still pass,
			// but that could simply be because we are not testing rigorously enough. it remains to be seen
			this.lastAcknowledgedEventId = this.lastSeenEventId;

			String jsonString = gson.toJson(fullJsonDataArray);

			RequestBody requestBody = RequestBody.create(jsonString, JSON);

			Request request = new Request.Builder()
					.url(this.getChannelUrl()) // todo maybe move to using `Headers` object
					.header("Content-Type", "application/json") // todo see what the difference between header and addHeader is
					.put(requestBody)
					.build();

			Response response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				System.err.println(requireNonNull(response.body()).string());
				throw new IOException("Error: " + response);
			}

			System.out.println(",============SendMessage============,");
			System.out.println("Id: " + jsonData.get("id").getAsInt());
			System.out.println("Sent message: " + fullJsonDataArray);
			System.out.println(".============SendMessage============.");

			return new InMemoryResponseWrapper(response);
		}
	}

	/**
	 * Pokes a ship with data.
	 *
	 * @param ship The ship to poke
	 * @param app  The app to poke
	 * @param mark The mark of the data being sent
	 * @param json The data to send
	 * @return a future poke response to the request
	 */
	public CompletableFuture<PokeResponse> poke(
			String ship,
			@NotNull String app,
			@NotNull String mark,
			@NotNull JsonElement json // todo maybe migrate type to JsonObject
	) throws IOException {

		// todo i think poke needs to return a completable future cause that seems to make more sense rather than taking in a pokeHandler...
		//  however, since this is working for now, we shouldn't do this until much later

		// according to https://gist.github.com/tylershuster/74d69e09650df5a86c4d8d8f00101b42#gistcomment-3477201
		//  you cannot poke a foreign ship with any other mark than json
		// also, urbit-airlock-ts seems to just use the connected ship here
		// and not really allow for that variation...

		// todo make poke strict to follow above rules

		CompletableFuture<PokeResponse> pokeFuture = new CompletableFuture<>();
		int id = nextID();
		JsonObject pokeDataObj = gson.toJsonTree(Map.of(
				"id", id,
				"action", "poke",
				"ship", ship,
				"app", app,
				"mark", mark,
				"json", json
		)).getAsJsonObject();
		// adapted from https://github.com/dclelland/UrsusAirlock/blob/master/Ursus%20Airlock/Airlock.swift#L114
		InMemoryResponseWrapper pokeResponse = this.sendJSONtoChannel(pokeDataObj);

		if (pokeResponse.getClosedResponse().isSuccessful()) {
			pokeHandlers.put(id, pokeFuture); // just incremented by sendJSONtoChannel
		}
//		pokeResponse.close();

		return pokeFuture;
	}

	/**
	 * Subscribes to a path on an app on a ship.
	 *
	 * @param ship The ship to subscribe to
	 * @param app  The app to subscribe to
	 * @param path The path to which to subscribe
	 * @return id of the subscription, which can be used to cancel it
	 */
	public int subscribe(
			@NotNull String ship,
			@NotNull String app,
			@NotNull String path,
			@NotNull Consumer<SubscribeEvent> subscribeHandler
	) throws IOException {
		int id = this.nextID();
		JsonObject subscribeDataObj;
		subscribeDataObj = gson.toJsonTree(Map.of(
				"id", id,
				"action", "subscribe",
				"ship", ship,
				"app", app,
				"path", path
		)).getAsJsonObject();
		InMemoryResponseWrapper subscribeResponse = this.sendJSONtoChannel(subscribeDataObj);

		if (subscribeResponse.getClosedResponse().isSuccessful()) {
			subscribeHandlers.put(id, subscribeHandler);
		}
//		subscribeResponse.close();

		return this.requestId;
	}

	/**
	 * Unsubscribes from a given subscription.
	 *
	 * @param subscription The id of the subscription to unsubscribe from
	 */
	public void unsubscribe(int subscription) throws IOException {
		int id = this.nextID();

		JsonObject unsubscribeDataObj = gson.toJsonTree(Map.of(
				"id", id,
				"action", "unsubscribe",
				"subscription", subscription
		)).getAsJsonObject();

		InMemoryResponseWrapper res = this.sendJSONtoChannel(unsubscribeDataObj);
//		res.close();
	}

	/**
	 * Deletes the connection to a channel.
	 */
	public void delete() throws IOException {
		int id = this.nextID();

		JsonObject deleteDataObj = gson.toJsonTree(Map.of(
				"id", id,
				"action", "delete"
		)).getAsJsonObject();

		InMemoryResponseWrapper res = this.sendJSONtoChannel(deleteDataObj);
//		res.close();
	}

	/**
	 * Deletes the connection to a channel.
	 */
	public void ack(int eventID) throws IOException {
		int id = this.nextID();

		JsonObject deleteDataObj = gson.toJsonTree(Map.of(
				"id", id,
				"action", "ack",
				"event-id", eventID
		)).getAsJsonObject();

		InMemoryResponseWrapper res = this.sendJSONtoChannel(deleteDataObj);
//		res.close();
	}


	public void scryRequest(String app, String path, String mark) {
		// todo impl alternative to sendJSONtoChannel
	}


	/**
	 * Utility function to connect to a ship that has its *.arvo.network domain configured.
	 *
	 * @param name Name of the ship e.g. zod
	 * @param code Code to log in
	 */
	@NotNull
	public static Urbit onArvoNetwork(String name, String code) {
		return new Urbit("https://" + name + ".arvo.network", name, code);
	}

	/**
	 * Returns a hex string of given length.
	 * <p>
	 * Poached from StackOverflow.
	 *
	 * @param len Length of hex string to return.
	 */
	private static String hexString(int len) {
		final int maxlen = 8;
		final double min = Math.pow(16, Math.min(len, maxlen) - 1);
		final double max = Math.pow(16, Math.min(len, maxlen)) - 1;
		final double n = Math.floor(Math.random() * (max - min + 1)) + min;

		StringBuilder r = new StringBuilder(Integer.toString((int) Math.round(n), 16));
		while (r.toString().length() < len) {
			r.append(Urbit.hexString(len - maxlen));
		}
		return r.toString();
	}

	/**
	 * Generates a random UID, urbit style
	 * <p>
	 * Copied from https://github.com/urbit/urbit/blob/137e4428f617c13f28ed31e520eff98d251ed3e9/pkg/interface/src/lib/util.js#L3
	 */
	static String uid() {
		// todo fix bug with implementation
		//  right now, the uid always has the first chunk as `0`, i.e. as in 0v1.0.3eolm.59lvl.7n9ht.2mokl.51js7
		//  also need to check for impl equivalence
		// (this is causing a hoon error when used)
		StringBuilder str = new StringBuilder("0v");
		str.append(Math.ceil(Math.random() * 8)).append('.');
		for (int i = 0; i < 5; i++) {
			String entropy = Integer.toString((int) Math.round(Math.ceil(Math.random() * 10000000)), 32); // todo check to see if this is equivalent with js number behaviours
			// pad entropy with zeroes
			entropy = "00000" + entropy;
			entropy = entropy.substring(entropy.length() - 5);
			str.append(entropy).append('.');
		}
		return str.substring(0, str.length() - 1);
	}
}
