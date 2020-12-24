package airlock;

import airlock.errors.*;
import airlock.errors.scry.ScryDataNotFoundException;
import airlock.errors.scry.ScryFailureException;
import airlock.errors.spider.SpiderFailureException;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
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
public class AirlockChannel {

	/**
	 * The code is the deterministic password used to authenticate with an Urbit ship
	 * It can be obtained by running `+code` in the dojo.
	 */
	private final String code;

	/**
	 * The URL representing the location where eyre is listening for requests on the ship
	 */
	private final URL url;


	/**
	 * A unique channel name. A channel name is typically the current unix time plus a random hex string
	 */
	private String channelID;

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
	 * The authentication cookie given to us after logging in with the {@link AirlockChannel#code}.
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
	 * This is the equivalent mapping for subscription handlers. See {@link AirlockChannel#pokeHandlers}.
	 */
	private final Map<Integer, Consumer<SubscribeEvent>> subscribeHandlers;

	public static final MediaType JSON
			= MediaType.get("application/json; charset=utf-8");

	private final OkHttpClient client;

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
	public AirlockChannel(URL url, String shipName, String code) {
		this.shipName = requireNonNull(shipName);
		this.code = requireNonNull(code, "Please provide a code");
		requireNonNull(url, "Please provide a url");
		this.url = AirlockUtils.normalizeOrBust(url);
		this.pokeHandlers = new HashMap<>();
		this.subscribeHandlers = new HashMap<>();
		this.cookie = null;
		this.channelID = generateChannelID();

		// init cookie manager to use `InMemoryCookieStore` by providing null
		CookieHandler cookieHandler = new CookieManager(null, CookiePolicy.ACCEPT_ALL);


		this.client = new OkHttpClient.Builder()
				.cookieJar(new JavaNetCookieJar(cookieHandler))
				.readTimeout(1, TimeUnit.DAYS)  // possible max length of session (time before we get an event back) (as per https://stackoverflow.com/a/47232731) // todo possibly adjust timeout duration might be too aggressive
				.build();

		// todo deduplicate network requests and error handling code

		// todo figure out what happens vs what should happen when:
		//  ship ~tun is running at localhost:80 and has code sampel-sampel
		//  you use the library, and make a new ship new Urbit("localhost:80", "~zod", "sampel-sample");
		//  you are now succesfully authenticated, but you have the wrong ship name. what do?
		//  ok so urbit 1.0 runs into a silent failure (on our end) and the helm hi fails when we use the ship tun while pretending our name is zod

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
	 *
	 * @return the URL to the unique channel created
	 */
	public URL getChannelUrl() {
		return AirlockUtils.resolveOrBust(this.url, "/~/channel/" + this.channelID);
	}

	@NotNull
	public URL getLoginUrl() {
		return AirlockUtils.resolveOrBust(this.url, "/~/login");
	}

	public URL getScryUrl(String app, String path, String mark) {
		return AirlockUtils.resolveOrBust(this.url, "/~/scry/" + app + "/" + path + "." + mark);
	}

	public URL getSpiderUrl(String inputMark, String threadName, String outputMark) {
		return AirlockUtils.resolveOrBust(this.url, "/spider/" + inputMark + "/" + threadName + "/" + outputMark + ".json");
	}

	/**
	 * Connects to the Urbit ship. Nothing can be done until this is called.
	 *
	 * @return Returns an immutable wrapper around a response body object
	 */
	public InMemoryResponseWrapper authenticate() throws AirlockRequestError, AirlockAuthenticationError {
		RequestBody formBody = new FormBody.Builder()
				.add("password", this.code)
				.build();

		Request request = new Request.Builder()
				.url(this.getLoginUrl())
				.post(formBody)
				.build();

		Response response = null;

		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			throw new AirlockRequestError("Failed to execute request", e);
		}

		if (!response.isSuccessful()) {
			throw new AirlockAuthenticationError("Got unsuccessful http response code", new IOException("Error: " + response));
		}

		// after we made the request, here we extract the cookie. it's quite ceremonial
		this.cookie = this.client.cookieJar().loadForRequest(requireNonNull(HttpUrl.get(this.getChannelUrl())))
				.stream()
				.filter(cookie1 -> cookie1.name().startsWith("urbauth"))
				.findFirst().orElseThrow(() -> new IllegalStateException("Did not receive valid authcookie"));
		// stream api is probably expensive and extra af but this is basically necessary to prevent brittle behavior

		return new InMemoryResponseWrapper(response);
	}


	/**
	 * Creates the channel on which the sseClient will be instantiated on
	 * This must be done in the same breath as creating the sseClient (i.e. in {@link AirlockChannel#connect()},
	 * otherwise we will never be able to create a connection to the ship.
	 *
	 */
	private void createChannel() throws AirlockResponseError, AirlockRequestError {
		JsonPrimitive jsonPayload = new JsonPrimitive("Opening Airlock :)");
		this.poke(this.getShipName(), "hood", "helm-hi", jsonPayload);
	}

	/**
	 * Initializes the SSE pipe for the appropriate channel (if necessary). Must be called after authenticating
	 */
	public void connect() throws AirlockResponseError, AirlockRequestError {
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
				.header("connection", "keep-alive") // todo why do i still have to manually set timeout to 1 day when connection is keep-alive
				.build();


		this.sseClient = EventSources.createFactory(this.client)
				.newEventSource(sseRequest, new EventSourceListener() {
					@Override
					public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
						int eventID = Integer.parseInt(requireNonNull(id, "Got null id")); // this thing is kinda useless

						synchronized (urbitLock) {
							EyreResponse eyreResponse = AirlockUtils.gson.fromJson(data, EyreResponse.class);
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
							} catch (AirlockChannelError e) {
								// even though normally a channel error is catch + recoverable,
								// if we cannot ack a message, we should fail because right now,
								// i do not know a valid way to continue in a useful state
								// so we terminate
								throw new IllegalStateException("could not ack");
							}


							//if (eyreResponse.id != eventD) {
							//throw new IllegalStateException("invalid ids or something");
							//}

							switch (eyreResponse.response) {
								case POKE:
									var pokeHandler = pokeHandlers.get(eyreResponse.id);
									if (eyreResponse.ok) {
										pokeHandler.complete(PokeResponse.SUCCESS);
									} else {
										pokeHandler.complete(PokeResponse.fromFailure(eyreResponse.err));
									}
									pokeHandlers.remove(eyreResponse.id);
									break;
								case SUBSCRIBE:
									var subscribeHandler = subscribeHandlers.get(eyreResponse.id);
									if (eyreResponse.ok) {
										subscribeHandler.accept(SubscribeEvent.STARTED);
									} else {
										subscribeHandler.accept(SubscribeEvent.fromFailure(eyreResponse.err));
										subscribeHandlers.remove(eyreResponse.id);
									}
									break;
								case DIFF:
									subscribeHandler = subscribeHandlers.get(eyreResponse.id);
									subscribeHandler.accept(SubscribeEvent.fromUpdate(eyreResponse.json));
									break;
								case QUIT:
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
						// todo better error handling
						//  right now, if I try to

						// here, we get an EOFException if we have a running connection and then Ctrl-z forcibly close the fakezod
						// so maybe that's another custom error to make
						// socket exception occurs because by default, the okhttp sse event client times out after like 500ms
						// if it hasn't received any data from the connection, even though that's normal when using eyre. this is why we set the timeout really high.

						if (t != null) {
							System.err.println("Encountered error while doing sse stuff");
							if (!(t instanceof SocketException)) {
								throw new RuntimeException(t);
							}
							System.err.println("Socket error");
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

						System.out.println("!!!!!!!!!!Closing!!!!!!!!!!!!");
						tearDown();
					}
				});
	}

	// todo change up api. this may be temporary
	public void tearDown() {


		channelID = AirlockChannel.uid();
		this.sseClient.cancel();
//		this.client.dispatcher().cancelAll(); // todo see if we need this or if it will cause more problems
		sseClient = null;
		requestId = 0;
		lastSeenEventId = 0;
		lastAcknowledgedEventId = 0;
		this.cookie = null;
		pokeHandlers.clear();
		subscribeHandlers.clear();
	}


	/**
	 * This is a wrapper method that can be used to send any action with data.
	 * <p>
	 * Every message sent has some common parameters, like method, headers, and data
	 * structure, so this method exists to prevent duplication.
	 * </p>
	 *
	 * @param jsonData The data to send with the action
	 * @return the response to the request
	 */
	public InMemoryResponseWrapper sendJSONtoChannel(JsonObject jsonData) throws AirlockResponseError, AirlockRequestError {
		synchronized (urbitLock) {
			JsonArray fullJsonDataArray = new JsonArray();
			JsonObject fullJsonData = jsonData.deepCopy(); // todo seems like a wasteful way to do it, if outside callers are using this method; possibly refactor
			//  if we make this method private then we can avoid this because we are the only ones ever calling the method so we can basically just make sure that we never call it with anything that we use later on that would be affected by the mutability of the json object
			fullJsonDataArray.add(fullJsonData);

			// todo is this correct behavior?? I just adapted it blindly-ish
			// commenting it out seems to not have an effect, i.e. tests still pass,
			// but that could simply be because we are not testing rigorously enough. it remains to be seen
			this.lastAcknowledgedEventId = this.lastSeenEventId;

			String jsonString = AirlockUtils.gson.toJson(fullJsonDataArray);

			RequestBody requestBody = RequestBody.create(jsonString, JSON);

			Request request = new Request.Builder()
					.url(this.getChannelUrl())
					.header("Content-Type", "application/json") // todo see what the difference between header and addHeader is
					.put(requestBody)
					.build();

			Response response = null;
			System.out.println(",============SendMessage============,");
			System.out.println("About the send the following message");
			System.out.println("Id: " + jsonData.get("id").getAsInt());
			System.out.println("Message: " + AirlockUtils.gson.toJson(fullJsonDataArray));
			System.out.println(".============SendMessage============.");
			try {
				response = client.newCall(request).execute();
			} catch (IOException e) {
				throw new AirlockRequestError("Unable to execute request", e);
			}

			InMemoryResponseWrapper responseWrapper = new InMemoryResponseWrapper(response);
			if (!response.isSuccessful()) {
				System.err.println(responseWrapper.getBody().utf8());
				throw new AirlockResponseError("Got unsuccessful http response code", new IOException("Error: " + response));
			}


			return responseWrapper;
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
	) throws AirlockResponseError, AirlockRequestError {

		// according to https://gist.github.com/tylershuster/74d69e09650df5a86c4d8d8f00101b42#gistcomment-3477201
		//  you cannot poke a foreign ship with any other mark than json
		// also, urbit-airlock-ts seems to just use the connected ship here
		// and not really allow for that variation...

		// todo make poke strict to follow above rules

		CompletableFuture<PokeResponse> pokeFuture = new CompletableFuture<>();
		int id = nextID();
		JsonObject pokeDataObj = AirlockUtils.gson.toJsonTree(Map.of(
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
	) throws AirlockResponseError, AirlockRequestError {
		int id = this.nextID();
		JsonObject subscribeDataObj;
		subscribeDataObj = AirlockUtils.gson.toJsonTree(Map.of(
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

		return this.requestId;
	}

	/**
	 * Unsubscribes from a given subscription.
	 *
	 * @param subscription The id of the subscription to unsubscribe from
	 */
	public void unsubscribe(int subscription) throws AirlockResponseError, AirlockRequestError {
		int id = this.nextID();

		JsonObject unsubscribeDataObj = AirlockUtils.gson.toJsonTree(Map.of(
				"id", id,
				"action", "unsubscribe",
				"subscription", subscription
		)).getAsJsonObject();

		this.sendJSONtoChannel(unsubscribeDataObj);
	}

	/**
	 * Deletes the connection to a channel.
	 */
	public void delete() throws AirlockResponseError, AirlockRequestError {
		int id = this.nextID();

		JsonObject deleteDataObj = AirlockUtils.gson.toJsonTree(Map.of(
				"id", id,
				"action", "delete"
		)).getAsJsonObject();

		this.sendJSONtoChannel(deleteDataObj);
	}

	/**
	 * Acks the given eventID
	 * @param eventID the id of the event to ack
	 */
	private void ack(int eventID) throws AirlockResponseError, AirlockRequestError {
		int id = this.nextID();

		JsonObject ackObj = AirlockUtils.gson.toJsonTree(Map.of(
				"id", id,
				"action", "ack",
				"event-id", eventID
		)).getAsJsonObject();

		this.sendJSONtoChannel(ackObj);
	}


	public JsonElement scryRequest(String app, String path) throws ScryDataNotFoundException, ScryFailureException, AirlockResponseError, AirlockAuthenticationError {
		// as per https://github.com/urbit/urbit/blob/90faac16c9f61278d0a1d946bd91c5b387f7a423/pkg/interface/src/logic/api/base.ts
		// we are never gonna use any other mark than json because that's the only protocol we know how to work with
		URL scryUrl = this.getScryUrl(app, path, "json");


		Request request = new Request.Builder()
				.url(scryUrl)
				.header("Content-Type", "application/json")
				.get()
				.build();

		Response response = null;
		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			throw new AirlockResponseError("Unable to execute request", e);
		}

		InMemoryResponseWrapper responseWrapper = new InMemoryResponseWrapper(response);
		String bodyText = responseWrapper.getBody().utf8();

		if (!response.isSuccessful()) {
			System.err.println(bodyText);
			this.throwOnScryFailure(response);
		}
		System.out.println(",============ScryRequest============,");
		System.out.println("Request: " + scryUrl);
		System.out.println(".============ScryRequest============.");

		return JsonParser.parseString(bodyText);
	}


	private void throwOnScryFailure(Response response) throws ScryFailureException, ScryDataNotFoundException, AirlockAuthenticationError {
		// assuming we receive a non-closed response object
		assert !response.isSuccessful();

		if (response.code() == 403) {
			throw new AirlockAuthenticationError("Got 403 when trying to make scry request.\n" + "Request: " + response.request() + "Response: " + response.body());
		} else if (response.code() == 404) {
			throw new ScryDataNotFoundException("Got 404 when trying to make scry request.\n" + "Request: " + response.request() + "Response: " + response.body());
		} else if (response.code() == 500) {
			throw new ScryFailureException("Got 500 when trying to make a request.\n" + "Request: " + response.request() + "Response: " + response.body());
		}
	}

	public JsonElement spiderRequest(String inputMark, String threadName, String outputMark, JsonObject jsonData) throws AirlockResponseError, AirlockRequestError, SpiderFailureException {

		// copied from sendJSONtoChannel
		// tbh I think that for now I'm only ever gonna be sending the json mark. so maybe I should just send

		String jsonString = AirlockUtils.gson.toJson(jsonData.deepCopy()); // todo possible refactor of deep copy

		RequestBody requestBody = RequestBody.create(jsonString, JSON);


		URL spiderUrl = this.getSpiderUrl(inputMark, threadName, outputMark);

		Request request = new Request.Builder()
				.url(spiderUrl)
				.header("Content-Type", "application/json")
				.post(requestBody)
				.build();

		Response response = null;
		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			throw new AirlockRequestError("Failed to execute request", e);
		}

		InMemoryResponseWrapper responseWrapper = new InMemoryResponseWrapper(response);
		String bodyText = responseWrapper.getBody().utf8();

		if (!response.isSuccessful()) {
			// 500 means there was an error doing the spider. add to custom errors
			// for example, trying to create a duplicate graph. in that case it doesn't seem to give a stack trace unlike the other times which was weird
			this.throwOnSpiderFailure(response);
			System.err.println(bodyText);
			throw new AirlockResponseError("Got unsuccessful http response code", new IOException("Error: " + response));
		}

		System.out.println(",============SpiderRequest============,");
		System.out.println("Request: " + spiderUrl);
		System.out.println("Payload: " + jsonString);
		System.out.println(".============SpiderRequest============.");


		return JsonParser.parseString(bodyText);


	}


	private void throwOnSpiderFailure(Response response) throws SpiderFailureException {
		// assuming we receive a non-closed response object
		assert !response.isSuccessful();

		if (response.code() == 500) {
			throw new SpiderFailureException("Got 500 when trying to make a request.\n" + "Request: " + response.request() + "\nResponse: \n" + response.body());
		}
	}



	/**
	 * Utility function to connect to a ship that has its *.arvo.network domain configured.
	 *
	 * @param name Name of the ship e.g. zod
	 * @param code Code to log in
	 */
	@NotNull
	public static AirlockChannel onArvoNetwork(String name, String code) {
		try {
			return new AirlockChannel(URI.create("https://" + name + ".arvo.network").toURL(), name, code);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Unable to create proper url from arvo.network");
		}
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
			r.append(AirlockChannel.hexString(len - maxlen));
		}
		return r.toString();
	}

	/**
	 * Generates a random UID, urbit style
	 * <p>
	 * Copied from https://github.com/urbit/urbit/blob/137e4428f617c13f28ed31e520eff98d251ed3e9/pkg/interface/src/lib/util.js#L3
	 */
	public static String uid() {
		// (this is causing a hoon error when used)
		StringBuilder str = new StringBuilder("0v");
		str.append((int) Math.ceil(Math.random() * 8)).append('.');

		for (int i = 0; i < 5; i++) {
			String entropy = Integer.toString((int) Math.round(Math.ceil(Math.random() * 10000000)), 32); // checked
			// pad entropy with zeroes
			entropy = "00000" + entropy;

			entropy = entropy.substring(entropy.length() - 5);

			str.append(entropy).append('.');

		}

		return str.substring(0, str.length() - 1);
	}

	@NotNull
	public static String generateChannelID() {
		return Math.round(Math.floor(Instant.now().toEpochMilli())) + "-" + AirlockChannel.hexString(6);
	}


}