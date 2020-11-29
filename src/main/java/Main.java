import com.google.gson.Gson;
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

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect();

		Main.test0(ship); // successful
		Main.test1(ship); // successful
		Main.test2(ship); // successful
		Main.test3(ship); // unsuccessful

		while(chatStoreEvents.size() < 2) {
			Thread.sleep(50);
		}
		System.out.println("finished. got the following events");
		System.out.println(chatStoreEvents);

		//Main.testChatView(ship);


	}

	public static void test0(Urbit ship) throws IOException {
		String json = "Opening airlock :)";
		ship.poke(ship.getShipName(), "hood", "helm-hi", json, pokeEvent -> {
			System.out.println("Got poke event");
			System.out.println(pokeEvent);
		});
	}

	public static void test1(Urbit ship) {
		// as per the guide, this code can only be called after
		// a channel has been created, which means only after the `helm-hi`
		ship.initEventSource();
	}

	public static void test2(Urbit ship) throws IOException, InterruptedException {
		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test2", subscribeEvent -> {
			System.out.println("Got Subscribe Event");
			System.out.println(subscribeEvent);
			chatStoreEvents.add(subscribeEvent);
		});


	}

	public static void test3(Urbit ship) throws IOException {
		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test2",
						"envelope", Map.of(
								"uid", Urbit.uid(),
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", "Hello, Mars!")
						)
				)
		);

		ship.poke(ship.getShipName(), "chat-hook", "json", gson.toJson(payload), System.out::println);

	}

	public static void testChatView(Urbit ship) throws IOException, InterruptedException {
		List<SubscribeEvent> events = new ArrayList<>();
		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			System.out.println("Got Subscribe Event");
			System.out.println(subscribeEvent);
			events.add(subscribeEvent);
		});
		while (events.isEmpty()) {
			Thread.sleep(1);
		}
	}

}
