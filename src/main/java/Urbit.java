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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public class Urbit {

	public static final MediaType JSON
			= MediaType.get("application/json; charset=utf-8");


	private final OkHttpClient client;

	/**
	 * Code is the deterministic password used to authenticate with an Urbit ship
	 * It can be obtained by running `+code` in the dojo.
	 */
	private String code;

	/**
	 * The location of the ship
	 */
	private String url;

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

	public EventSource getSseClient() {
		// todo: evaluate if this is necessary because the object doesn't seem all that useful
		return sseClient;
	}

	/**
	 * The authentication cookie given to us after logging in with the {@link Urbit#code}.
	 * Note: it is possible to authenticate with an incorrect +code and still get an authcookie.
	 * Only after sending the first real request will it fail.
	 */
	private String cookie;

	/**
	 * The name of the ship that we are connecting to. (the @p without '~')
	 */
	private final String shipName;

	public String getShipName() {
		return shipName;
	}

	/**
	 * This is a Map between event-id of a poke request and the respective handler function.
	 * When the sseClient receives an {@link EyreResponse}, it propagates the data in the form of a {@link PokeEvent}
	 * to the correct handler function.
	 */
	private final Map<Integer, Consumer<PokeEvent>> pokeHandlers;

	/**
	 * This is the equivalent mapping for subscription handlers. See {@link Urbit#pokeHandlers}.
	 */
	private final Map<Integer, Consumer<SubscribeEvent>> subscribeHandlers;


	private final Gson gson;

	private final Object urbitLock = new Object();


	/**
	 * Constructs a new Urbit connection.
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
		this.shipName = requireNonNullElse(shipName, "");

		// init cookie manager
		CookieHandler cookieHandler = new CookieManager(null, CookiePolicy.ACCEPT_ALL);


		this.client = new OkHttpClient.Builder()
//				.cookieJar(new JavaNetCookieJar(cookieHandler)) // TODO enable and test this with next iteration
				.readTimeout(1, TimeUnit.DAYS)  // possible max length of session (time before we get an event back) (as per https://stackoverflow.com/a/47232731) // todo possibly adjust timeout duration might be too aggressive
				.build();

		gson = new Gson();

		// TODO: Instead of returning the response object, which is kind of useless, return a {Completeable}Future<T> for pokes at least, not valid for subscribes
		// todo, use `Cookie` and CookieJar and stuff if necessary in the future. for now it's an overkill
		// todo, see if we want to punt up the IOException to the user or just consume it within the API or even make a custom exception (may be overkill).
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


	/**
	 * Connects to the Urbit ship. Nothing can be done until this is called.
	 */
	public Response connect() throws IOException {
		RequestBody formBody = new FormBody.Builder()
				.add("password", this.code)
				.build();

		Request request = new Request.Builder()
				.header("connection", "keep-alive")
				.url(this.getLoginUrl())
				.post(formBody)
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) throw new IOException("Error: " + response);
		// todo figure out best way to return an immutable responsebody obj or something
		//  or design api around it or use different library.
		// basically, response.body() is a one-shot obj that needs to be copied manually so it sucks
		// we can't call it multiple times

		String cookieString = requireNonNull(response.header("set-cookie"), "No cookie given");
		Cookie cookie = Cookie.parse(request.url(), cookieString);
		requireNonNull(cookie, "Unable to parse cookie from string:" + cookieString);
		this.cookie = cookie.name() + "=" + cookie.value();

		return response; // TODO Address possible memory leak with returning unclosed response object
	}


	/**
	 * Initializes the SSE pipe for the appropriate channel (if necessary)
	 */
	void initEventSource() {
		if (this.sseClient != null) {
			return;
		}
		Request sseRequest = new Request.Builder()
				.url(this.getChannelUrl())
				.header("Cookie", this.cookie)
				.header("connection", "keep-alive")
				.build();

		this.sseClient = EventSources.createFactory(this.client)
				.newEventSource(sseRequest, new EventSourceListener() {
					@Override
					public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
						int eventID = Integer.parseInt(requireNonNull(id, "Got null id")); // this thing is kinda useless
						//lastSeenEventId = eventID; // todo should this be from eyre payload

						synchronized (urbitLock) {
							EyreResponse eyreResponse = gson.fromJson(data, EyreResponse.class);


							lastSeenEventId = eyreResponse.id;

							try {
								if (lastSeenEventId != lastAcknowledgedEventId) {
									ack(lastSeenEventId);
									lastAcknowledgedEventId = lastSeenEventId;
								}
							} catch (IOException e) {
								throw new IllegalStateException("could not ack");
							}

							System.out.println(",=============Event==============,");
							System.out.println("raw: " + data);
							System.out.println("event id from okhttp " + eventID);
							System.out.println("lastSeenEventId: " + lastSeenEventId);
							System.out.println("lastAckedEventId: " + lastAcknowledgedEventId);
							System.out.println("got eyre response data");
							System.out.println(eyreResponse);
							System.out.println(".=============Event==============.");


							//if (eyreResponse.id != eventD) {
							//throw new IllegalStateException("invalid ids or something");
							//}

							switch (eyreResponse.response) {
								case "poke":
									var pokeHandler = pokeHandlers.get(eyreResponse.id);
									if (eyreResponse.isOk()) {
										pokeHandler.accept(PokeEvent.SUCCESS);
									} else {
										pokeHandler.accept(PokeEvent.fromFailure(eyreResponse.err));
									}
									pokeHandlers.remove(eyreResponse.id);
									break;
								case "subscribe":
									var subscribeHandler = subscribeHandlers.get(eyreResponse.id);
									if (eyreResponse.isOk()) {
										subscribeHandler.accept(SubscribeEvent.STARTED);
									} else {
										subscribeHandler.accept(SubscribeEvent.fromFailure(eyreResponse.err));
										subscribeHandlers.remove(eyreResponse.id); // haha whoops :p
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
							//							System.out.println(t.getMessage()); // omg im so stupid i should've been printing this earlier
//							if (response != null) {
//								System.out.println("response");
//								try {
//									System.out.println(requireNonNull(response.body()).string());
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
//							}
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
	 *
	 * Every message sent has some common parameters, like method, headers, and data
	 * structure, so this method exists to prevent duplication.
	 *
	 * @param jsonData The data to send with the action
	 */
	public Response sendJSONtoChannel(JsonObject jsonData) throws IOException {
		synchronized (urbitLock) {
			JsonArray fullJsonDataArray = new JsonArray();
			JsonObject fullJsonData = jsonData.deepCopy(); // todo seems like a wasteful way to do it, if outside callers are using this method; possibly refactor
			//  if we make this method private then we ca avoid this because we are the only ones ever calling the method so we can bascially ejust make sure that we never call it with anything that we use later on that would be affected by the mutablity of the jsonobject
			fullJsonDataArray.add(fullJsonData);

//		// acknowledge last seen event
			System.out.println("last ack != last seen: " + (lastAcknowledgedEventId != lastSeenEventId));
		/*if (lastAcknowledgedEventId != lastSeenEventId) {
			JsonObject ackObj = new JsonObject();
			ackObj.addProperty("action", "ack");
			ackObj.addProperty("event-id", this.lastSeenEventId);
//			System.out.println("Last acked id: " + lastAcknowledgedEventId);
//			System.out.println("Acking id: " + lastSeenEventId);
			fullJsonDataArray.add(ackObj);
			lastAcknowledgedEventId = lastSeenEventId;
		}*/

			this.lastAcknowledgedEventId = this.lastSeenEventId;

			String jsonString = gson.toJson(fullJsonDataArray);

			RequestBody requestBody = RequestBody.create(jsonString, JSON);

			Request request = new Request.Builder()
					.url(this.getChannelUrl())
					.header("Cookie", this.cookie) // todo maybe move to using `Headers` object
					.header("Connection", "keep-alive") // todo see what the difference between header and addHeader is
					.header("Content-Type", "application/json")
					.put(requestBody)
					.build();

			Response response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				System.err.println(requireNonNull(response.body()).string());
				throw new IOException("Error: " + response);
			}

			System.out.println("=============SendMessage=============");
			System.out.println("Id: " + jsonData.get("id").getAsInt());
			System.out.println("Sent message: " + fullJsonDataArray);
			System.out.println("=============SendMessage=============");

			return response; // TODO Address possible memory leak with returning unclosed response object
		}
	}

	/**
	 * Pokes a ship with data.
	 * @param ship The ship to poke
	 * @param app  The app to poke
	 * @param mark The mark of the data being sent
	 * @param json The data to send
	 */
	public void poke(
			String ship,
			@NotNull String app,
			@NotNull String mark,
			@NotNull JsonElement json, // todo maybe migrate type to JsonObject
			@NotNull Consumer<PokeEvent> pokeHandler
	) throws IOException {

		// according to https://gist.github.com/tylershuster/74d69e09650df5a86c4d8d8f00101b42#gistcomment-3477201
		//  you cannot poke a foreign ship with any other mark than json
		// also, urbit-airlock-ts seems to just use the connected ship here
		// and not really allow for that variation...

		// todo make poke strict to follow above rules

		JsonObject pokeDataObj;
		int id = nextID();
		pokeDataObj = gson.toJsonTree(Map.of(
				"id", id,
				"action", "poke",
				"ship", ship,
				"app", app,
				"mark", mark,
				"json", json
		)).getAsJsonObject();
		// adapted from https://github.com/dclelland/UrsusAirlock/blob/master/Ursus%20Airlock/Airlock.swift#L114
		Response pokeResponse = this.sendJSONtoChannel(pokeDataObj);

		if (pokeResponse.isSuccessful()) {
			System.out.println("registering poke handler for id: " + id);
			pokeHandlers.put(id, pokeHandler); // just incremented by sendJSONtoChannel
		}
		pokeResponse.close();
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
		Response subscribeResponse = this.sendJSONtoChannel(subscribeDataObj);
//		System.out.println("subscribe response is succcesful");
//		System.out.println(subscribeResponse.isSuccessful());
		if (subscribeResponse.isSuccessful()) {
//			System.out.println("registering handler for id: " + id);
			subscribeHandlers.put(id, subscribeHandler);
		}
		subscribeResponse.close();

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

		Response res = this.sendJSONtoChannel(unsubscribeDataObj);
		res.close();
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

		Response res = this.sendJSONtoChannel(deleteDataObj);
		res.close();
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

		Response res = this.sendJSONtoChannel(deleteDataObj);
		res.close();
	}


	// TODO add scry and spider requests

	/**
	 * Utility function to connect to a ship that has its *.arvo.network domain configured.
	 *
	 * @param name Name of the ship e.g. zod
	 * @param code Code to log in
	 */
	@NotNull
	static Urbit onArvoNetwork(String name, String code) {
		return new Urbit("https://" + name + ".arvo.network", name, code);
	}

	/**
	 * Returns a hex string of given length.
	 * <p>
	 * Poached from StackOverflow.
	 *
	 * @param len Length of hex string to return.
	 */
	static String hexString(int len) {
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
	 * Generates a random UID.
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
