import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static java.util.Map.entry;

public class Main {

	private static final Gson gson = new Gson();
	private static final List<SubscribeEvent> chatStoreEvents = new ArrayList<>();
	private static final List<SubscribeEvent> primaryChatViewEvents = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect();

		Main.test0(ship); // successful
		Main.test1(ship); // successful
		Main.test2(ship); // successful
		Main.test3(ship); // successful

		while (chatStoreEvents.size() < 2) {
			Thread.sleep(50);
		}
		System.out.println("finished. got the following events");
		System.out.println(chatStoreEvents);

		//Main.testChatView(ship);


	}

	public static void test0(Urbit ship) throws IOException {
		JsonPrimitive jsonPayload = new JsonPrimitive("Opening Airlock :)");
		ship.poke(ship.getShipName(), "hood", "helm-hi", jsonPayload, pokeEvent -> {
			System.out.println("Got poke event");
			System.out.println(pokeEvent);
		});
	}

	public static void test1(Urbit ship) {
		// as per the guide, this code can only be called after
		// a channel has been created, which means only after the `helm-hi`
		ship.initEventSource();
	}

	public static void test2(Urbit ship) throws IOException {
		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test2", subscribeEvent -> {
			System.out.println("Subscribe Event");
			System.out.println(subscribeEvent);
			chatStoreEvents.add(subscribeEvent);
		});


	}

	public static void test3(Urbit ship) throws IOException {
		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test2",
						"envelope", Map.of(
//								"uid", Urbit.uid(),
								"uid", "0v1.00000.3eolm.59lvl.7n9ht.2mokl.51js7",
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", "Hello, Mars! It is now " + Instant.now().toString())
						)
				)
		);

		JsonElement payloadJSON = gson.toJsonTree(payload);
		ship.poke(ship.getShipName(), "chat-hook", "json", payloadJSON, pokeEvent -> {
			if (pokeEvent.success) {
				System.out.println("[PokeHandler]: successfully poked message to mailbox");
			} else {
				System.out.println("[PokeHandler]: could not poke message to mailbox");
				System.out.println("[PokeHandler]: failureMessage: ");
				System.out.println(pokeEvent.failureMessage);
			}
		});

	}

	public static void testChatView(Urbit ship) throws IOException {
		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			System.out.println("Subscribe Event");
			System.out.println(subscribeEvent);
			primaryChatViewEvents.add(subscribeEvent);
		});
	}

}
