import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTests {

	private static Gson gson;
	private static Urbit ship;
	private static List<PokeResponse> sendChatMessageResponses;
	private static List<SubscribeEvent> subscribeToMailboxEvents;
	private static List<SubscribeEvent> primaryChatSubscriptionEvents;
	private final String primaryChatViewTestMessage = "Primary Chat view Test Message" + Instant.now().toEpochMilli();


	/* TODOs
	* TODO add tests for subscription canceling and various other parts of the existing api
	 */


	@BeforeAll
	public static void setup() {
		int port = 8080;
		String url = "http://localhost:" + port;
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		ship = new Urbit(url, shipName, code);
		subscribeToMailboxEvents = new ArrayList<>();
		sendChatMessageResponses = new ArrayList<>();
		primaryChatSubscriptionEvents = new ArrayList<>();
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
			subscribeToMailboxEvents.add(subscribeEvent);
		});

		await().until(() -> subscribeToMailboxEvents.size() >= 2);
		assertEquals(SubscribeEvent.EventType.STARTED, subscribeToMailboxEvents.get(0).eventType);
		// todo add assertion for the second event
	}


	@Test
	@Order(4)
	public void canSendChatMessage() throws IOException {
		await().until(ship::isConnected);
		await().until(() -> !subscribeToMailboxEvents.isEmpty());

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

	@Test
	@Order(5)
	public void testChatView() throws IOException, ExecutionException, InterruptedException {
		await().until(ship::isConnected);

		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			primaryChatSubscriptionEvents.add(subscribeEvent);
		});

		// send a message to a chat that we haven't subscribed to already
		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test", // different chat
						"envelope", Map.of(
//								"uid", Urbit.uid(),
								"uid", "0v1.00001.3eolm.59lvl.7n9ht.2mokl.51js7",
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", primaryChatViewTestMessage)
						)
				)
		);

		CompletableFuture<PokeResponse> pokeFuture = new CompletableFuture<>();
		ship.poke(ship.getShipName(), "chat-hook", "json", gson.toJsonTree(payload), pokeFuture::complete);
		await().until(pokeFuture::isDone);
		assertTrue(pokeFuture.get().success);

		// wait until we have at least one proper "chat-update" message that isn't just the initial 20 messages sent
		await().until(
				() -> primaryChatSubscriptionEvents
						.stream()
						.anyMatch(onlyPrimaryChatUpdate())
		);
		primaryChatSubscriptionEvents.stream().
				filter(onlyPrimaryChatUpdate())
				.findFirst()
				.ifPresentOrElse(subscribeEvent -> {
					String message = subscribeEvent.updateJson
							.getAsJsonObject("chat-update")
							.getAsJsonObject("message")
							.getAsJsonObject("envelope")
							.getAsJsonObject("letter")
							.get("text")
							.getAsString();
					assertEquals(message, primaryChatViewTestMessage);
				}, () -> fail("Chat message received was not the same as the one sent"));

	}

	@NotNull
	public Predicate<SubscribeEvent> onlyPrimaryChatUpdate() {
		return subscribeEvent -> subscribeEvent.eventType.equals(SubscribeEvent.EventType.UPDATE)  // are an update event
				&& subscribeEvent.updateJson.has("chat-update")                 // and the update json contains a "chat-update" object
				&& subscribeEvent.updateJson.getAsJsonObject("chat-update").has("message");
	}

}
