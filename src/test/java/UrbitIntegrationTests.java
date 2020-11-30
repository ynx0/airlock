import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTests {

	private static Urbit ship;
	private static List<SubscribeEvent> subcribeToMailboxEvents;
	private static Gson gson;
	private static List<PokeResponse> sendChatMessageResponses;


	@BeforeAll
	public static void setup() {
		int port = 8080;
		String url = "http://localhost:" + port;
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		ship = new Urbit(url, shipName, code);
		subcribeToMailboxEvents = new ArrayList<>();
		sendChatMessageResponses = new ArrayList<>();
		gson = new Gson();
	}

	@Test
	@Order(1)
	public void successfulAuthentication() {
		assertDoesNotThrow(() -> ship.authenticate());
	}

	@Test
	@Order(2)
	public void successfullyConnectToShip() {
		await().until(ship::isAuthenticated);
		assertDoesNotThrow(() -> ship.connect());
	}


	@Test
	@Order(3)
	public void canSubscribeToTestChat() throws IOException {
		await().until(ship::isConnected);

		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test2", subscribeEvent -> {
			subcribeToMailboxEvents.add(subscribeEvent);
		});

		await().until(() -> subcribeToMailboxEvents.size() >= 2);
		assertEquals(SubscribeEvent.EventType.STARTED, subcribeToMailboxEvents.get(0).eventType);
		// todo add assertion for the second event
	}


	@Test
	@Order(4)
	public void canSendChatMessage() throws IOException {
		await().until(ship::isConnected);
		await().until(() -> !subcribeToMailboxEvents.isEmpty());

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

		ship.poke(ship.getShipName(), "chat-hook", "json", gson.toJsonTree(payload), pokeResponse -> sendChatMessageResponses.add(pokeResponse));

		await().until(() -> !sendChatMessageResponses.isEmpty());

		assertTrue(sendChatMessageResponses.get(0).success);
	}


	public static void testChatView(Urbit ship) throws IOException {

		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			System.out.println("[Primary Subscribe Event]");
			System.out.println(subscribeEvent);
//			primaryChatViewEvents.add(subscribeEvent);
		});
		System.out.println("Chat View Subscription ID: " + subscriptionID);
	}



}
