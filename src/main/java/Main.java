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

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect();

		Main.test0(ship); // successful
		Main.test1(ship); // successful
		Main.test2(ship); // successful but no events back
		Main.test3(ship); // successful but no events back

		Main.testChatView(ship);

//		var daemon = new Thread(() -> {
//			try {
//				Main.testChatView(ship);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		});
//		daemon.setDaemon(true);
//		daemon.start();
//		Thread.sleep(50000);

//		System.out.println("Done communicating with mars.");


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

	public static void test2(Urbit ship) throws IOException {
		Response res = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test2", subscribeEvent -> {
			System.out.println("Got Subscribe Event");
			System.out.println(subscribeEvent);
		});
		String body = Objects.requireNonNull(res.body()).string();
		System.out.println(body); // should be empty
	}

	public static void testChatView(Urbit ship) throws IOException, InterruptedException {
		List<SubscribeEvent> events = new ArrayList<>();
		Response res = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			System.out.println("Got Subscribe Event");
			System.out.println(subscribeEvent);
			events.add(subscribeEvent);
		});
		while (events.isEmpty()) {
			Thread.sleep(1);
		}
	}

	public static void test3(Urbit ship) throws IOException {


		Map<String, Object> payload = new HashMap<>();


		payload = Map.ofEntries(
				entry("message", Map.ofEntries(
						entry("path", "/~zod/test2"),
						entry("envelope", Map.of(
								"uid", Urbit.uid(),
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", "Hello, Mars!")
						))
				))
		);

		ship.poke(ship.getShipName(), "chat-hook", "json", gson.toJson(payload), System.out::println);

	}

}
