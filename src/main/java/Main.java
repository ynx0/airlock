import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("BusyWait")
public class Main {

	private static final Gson gson = new Gson();
	private static final List<SubscribeEvent> chatStoreEvents = new ArrayList<>();
	private static final List<SubscribeEvent> chatViewEvents = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:8080";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";


		// SETUP
		// The following example assumes you have:
		// - a ship named 'zod' running
		// - a chat channel called 'test' (you must manually create this)


		// MARK - ship setup
		Urbit ship = new Urbit(url, shipName, code);
		ship.authenticate(); // submit the code to the ship for authentication. must be done before anything else
		ship.connect();      // establishes the ServerSideEvent (SSE) client. this is what is used to receive all responses from the ship

		// MARK - create a 'mailbox' subscription on the chat-store gall agent
		int chatStoreSubscriptionID = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test", subscribeEvent -> {
			System.out.println("[Subscribe Event]");
			System.out.println(subscribeEvent);
			// store each event to a list
			chatStoreEvents.add(subscribeEvent);
		});


		// MARK - Send a chat message on the channel 'test'
		// create the json payload
		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test",
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

		// convert json payload to JsonElement
		JsonElement payloadJSON = gson.toJsonTree(payload);

		// make a poke request to the "chat-hook" app with the message as the payload, "json" being the payload type
		ship.poke(ship.getShipName(), "chat-hook", "json", payloadJSON).whenComplete(
				(pokeResponse, throwable) -> {
					if (pokeResponse.success) {
						System.out.println("[PokeHandler]: successfully poked message to mailbox");
					} else {
						System.out.println("[PokeHandler]: could not poke message to mailbox. Failed with message: \n" + pokeResponse.failureMessage);
					}
				}
		);

		// wait for events to populate in the list
		while (chatStoreEvents.size() < 2) {
			Thread.sleep(50);
		}
		System.out.println("got the following events from chat-store, with subscription id:" + chatStoreSubscriptionID);
		chatStoreEvents.forEach(System.out::println);

		// MARK - create a subscription on the path "/primary" on the chat-view gall agent
		int chatViewSubscriptionID = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			System.out.println("[Primary Subscribe Event]");
			System.out.println(subscribeEvent);
			chatViewEvents.add(subscribeEvent);
		});

		while (chatViewEvents.size() < 2) {
			Thread.sleep(50);
		}
		System.out.println("go the following events from chat-store, with subscription id: " + chatViewSubscriptionID);
		chatViewEvents.forEach(System.out::println);

	}


}
