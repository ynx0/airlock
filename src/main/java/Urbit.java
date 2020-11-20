import com.google.gson.Gson;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static java.util.Objects.*;
import static java.util.Objects.requireNonNull;

public class Urbit {

	public static final MediaType JSON
			= MediaType.get("application/json; charset=utf-8");


	private final OkHttpClient client;
	/**
	 * Code is the deterministic password
	 */
	private String code;

	/**
	 * URL is the location of the instance.
	 */
	private String url;

	/**
	 * UID will be used for the channel: The current unix time plus a random hex string
	 */
	private String uid;

	/**
	 * Last Event ID is an auto-updated index of which events have been sent over this channel
	 */
	private int lastEventId = 0;

	/**
	 * SSE Client is null for now; we don't want to start polling until it the channel exists
	 */
	private EventSource sseClient;

	/**
	 * Cookie gets set when we log in.
	 */
	private String cookie;

	/**
	 * Ship can be set, in which case we can do some magic stuff like send chats
	 */
	private final String ship;


	private final Gson gson;


	/**
	 * Constructs a new Urbit connection.
	 *
	 * @param url  The URL (with protocol and port) of the ship to be accessed
	 * @param code The access code for the ship at that address
	 */
	public Urbit(String url, String ship, String code) {
		this.uid = Math.floor(Instant.now().toEpochMilli())+ "-" + Urbit.hexString(6);
		this.code = code;
		this.url = url;

		this.client = new OkHttpClient();
		this.initEventSource();
		this.ship = requireNonNullElse(ship, "");

		gson = new Gson();


		// todo, use `Cookie` and CookieJar and stuff if necessary in the future. for now it's an overkill
		// todo, see if we want to punt up the IOException to the user or just consume it within the API or even make a custom exception (may be overkill).
	}

	/** This is basic interpolation to get the channel URL of an instantiated Urbit connection. */
	public String channelUrl() {
		return this.url + "/~/channel/" + this.uid;
	}
	/**
	 * Connects to the Urbit ship. Nothing can be done until this is called.
	 */
	public Response connect() throws Exception {
		RequestBody formBody = new FormBody.Builder()
				.add("password", this.code)
				.build();

		Request request = new Request.Builder()
			.header("connection", "keep-alive")
			.url(this.url + "/~/login")
			.post(formBody)
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) throw new IOException("Error: " + response);

			// todo remove ugly requireNonNull or migrate to kotlin lmao
			System.out.println(requireNonNull(response.body(), "No response body").string());
			this.cookie = requireNonNull(response.header("set-cookie"), "No cookie given"); // todo check if this is analogous to typescript version with just string
			return response;
		}
	}

	/**
	 * Returns (and initializes, if necessary) the SSE pipe for the appropriate channel.
	 */
	void initEventSource() {
		if (this.sseClient != null) {
			return;
		}
		Request sseRequest = new Request.Builder()
				.url(this.channelUrl())
				.header("connection", "keep-alive")
				.build();
		this.sseClient = EventSources.createFactory(this.client)
						.newEventSource(sseRequest, new EventSourceListener() {
							@Override
							public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
								super.onEvent(eventSource, id, type, data); // todo see if we need this or not
								try {
									ack(lastEventId);
								} catch (IOException e) {
									// todo make less ugly?
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
								super.onFailure(eventSource, t, response);
								System.err.println("Event Source Error: " + response);
							}
						});
	}



	/**
	 * Returns the next event ID for the appropriate channel.
	 */
	int getEventId() {
		this.lastEventId++;
		return this.lastEventId;
	}

	/**
	 * Acknowledges an event.
	 *
	 * @param eventId The event to acknowledge.
	 */
	public Response ack(int eventId) throws IOException {
		return this.sendMessage("ack", null/*{"event-id": eventId }*/);
	}

	/**
	 * This is a wrapper method that can be used to send any action with data.
	 *
	 * Every message sent has some common parameters, like method, headers, and data
	 * structure, so this method exists to prevent duplication.
	 *
	 * @param action The action to send
	 * @param jsonData The data to send with the action
	 */
	public Response sendMessage(String action, Collection<Object> jsonData) throws IOException {

		// todo append `id` and `action` to jsonData before creating reqbody
		RequestBody requestBody = RequestBody.create(gson.toJson(jsonData), JSON);


		Request request = new Request.Builder()
				.url(this.channelUrl())
				.header("Cookie", this.cookie) // todo maybe move to using `Headers` object
				.header("connection", "keep-alive") // todo see what the difference between header and addHeader is
				.header("Content-Type", "application/json")
				.put(requestBody)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) throw new IOException("Error: " + response);
			System.out.println(requireNonNull(response.body(), "No response body").string());

			return response;
		}
	}

	/**
	 * Pokes a ship with data.
	 *
	 * @param ship The ship to poke
	 * @param app The app to poke
	 * @param mark The mark of the data being sent
	 * @param json The data to send
	 */
	public Response poke(
			@Nullable String ship,
			@NotNull String app,
			@NotNull String mark,
			@NotNull Object json
	) throws IOException {

		ship = requireNonNullElse(ship, this.ship);
		return this.sendMessage("poke", Arrays.asList(ship, app, mark, json));
	}

	/**
	 * Subscribes to a path on an app on a ship.
	 *
	 * @param ship The ship to subscribe to
	 * @param app The app to subsribe to
	 * @param path The path to which to subscribe
	 */
	public Response subscribe(
			@Nullable String ship,
			@NotNull String app,
			@NotNull String path
	) throws IOException {
		ship = requireNonNullElse(ship, this.ship);
		return this.sendMessage("subscribe", Arrays.asList(ship, app, path));
	}

	/**
	 * Unsubscribes to a given subscription.
	 *
	 * @param subscription
	 */
	public Response unsubscribe(String subscription) throws IOException {
		return this.sendMessage("unsubscribe", Collections.singleton(subscription));
	}

	/**
	 * Deletes the connection to a channel.
	 */
	public Response delete() throws IOException {
		return this.sendMessage("delete", Collections.EMPTY_LIST);
	}

	/**
	 * Utility function to connect to a ship that has its *.arvo.network domain configured.
	 *
	 * @param name Name of the ship e.g. zod
	 * @param code Code to log in
	 */
	@NotNull
	static Urbit onArvoNetwork(String name, String code) {
		return new Urbit("https://" +  name + ".arvo.network", name, code);
	}

	/**
	 * Returns a hex string of given length.
	 *
	 * Poached from StackOverflow.
	 *
	 * @param len Length of hex string to return.
	 */
	static String hexString(int len) {
		final int maxlen = 8;
		final double min = Math.pow(16, Math.min(len, maxlen) - 1);
		final double max = Math.pow(16, Math.min(len, maxlen)) - 1;
		final double n = Math.floor(Math.random() * (max - min + 1)) + min;

		StringBuilder r = new StringBuilder(Integer.toString((int) n, 16));
		while (r.toString().length() < len) {
			r.append(Urbit.hexString(len - maxlen));
		}
		return r.toString();
	}

	/**
	 * Generates a random UID.
	 *
	 * Copied from https://github.com/urbit/urbit/blob/137e4428f617c13f28ed31e520eff98d251ed3e9/pkg/interface/src/lib/util.js#L3
	 */
	static String uid() {
		StringBuilder str = new StringBuilder("0v");
		str.append(Math.ceil(Math.random() * 8)).append('.');
		for (int i = 0; i < 5; i++) {
			String _str = Integer.toString((int) Math.ceil(Math.random() * 10000000), 32); // todo check to see if this is equivalent with js number behaviours
			_str = ("00000" + _str).substring(-5, 5);
			str.append(_str).append('.');
		}
		return str.substring(0, str.length() - 1);
	}
}
