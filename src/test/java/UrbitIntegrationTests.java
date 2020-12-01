import airlock.InMemoryResponseWrapper;
import airlock.PokeResponse;
import airlock.SubscribeEvent;
import airlock.Urbit;
import airlock.app.chat.ChatUpdate;
import airlock.app.chat.ChatUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTests {

	private static Gson gson;
	private static Urbit ship;
	private static CompletableFuture<PokeResponse> chatPokeResponse1;
	private static List<SubscribeEvent> subscribeToMailboxEvents;
	private static List<SubscribeEvent> primaryChatSubscriptionEvents;
	private final String primaryChatViewTestMessage = "Primary Chat view Test Message" + Instant.now().toEpochMilli();


	Predicate<SubscribeEvent> onlyPrimaryChatUpdate = subscribeEvent ->
			subscribeEvent.eventType.equals(SubscribeEvent.EventType.UPDATE)  // are an update event
					&& subscribeEvent.updateJson.has("chat-update")                 // and the update json contains a "chat-update" object
					&& subscribeEvent.updateJson.getAsJsonObject("chat-update").has("message");


	/* TODOs
	 * TODO add tests for subscription canceling
	 * TODO test manually canceling eventsource / deleting channel
	 */


	@BeforeAll
	public static void setup() {
		int port = 8080;
		String url = "http://localhost:" + port;
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		ship = new Urbit(url, shipName, code);
		subscribeToMailboxEvents = new ArrayList<>();
		primaryChatSubscriptionEvents = new ArrayList<>();
		gson = new Gson();

		// Assumes chat channel called 'test' is created

	}

	@Test
	@Order(1)
	public void successfulAuthentication() throws ExecutionException, InterruptedException {
		CompletableFuture<String> futureResponseString = new CompletableFuture<>();
		assertDoesNotThrow(() -> {
			InMemoryResponseWrapper res = ship.authenticate();
//			System.out.println("String resp body of auth");
			futureResponseString.complete(res.getBody().utf8());
		});
		await().until(futureResponseString::isDone);
		assertEquals("", futureResponseString.get());
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

		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test", subscribeEvent -> {
			subscribeToMailboxEvents.add(subscribeEvent);
		});

		await().until(() -> subscribeToMailboxEvents.size() >= 2);
		assertEquals(SubscribeEvent.EventType.STARTED, subscribeToMailboxEvents.get(0).eventType);
		// todo add assertion for the second event
	}


	@Test
	@Order(4)
	public void canSendChatMessage() throws IOException, ExecutionException, InterruptedException {
		await().until(ship::isConnected);
		await().until(() -> !subscribeToMailboxEvents.isEmpty());

		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test",
						"envelope", Map.of(
								"uid", Urbit.uid(),
//								"uid", "0v1.00000.3eolm.59lvl.7n9ht.2mokl.51js7",
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", "Hello, Mars! It is now " + Instant.now().toString())
						)
				)
		);

		chatPokeResponse1 = ship.poke(ship.getShipName(), "chat-hook", "json", gson.toJsonTree(payload));
		await().until(chatPokeResponse1::isDone);

		assertTrue(chatPokeResponse1.get().success);
	}

	@Test
	@Order(5)
	public void testChatView() throws IOException, ExecutionException, InterruptedException {
		await().until(ship::isConnected);
		await().until(chatPokeResponse1::isDone);


		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			primaryChatSubscriptionEvents.add(subscribeEvent);
		});

		// send a message to a chat that we haven't subscribed to already
		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test", // different chat
						"envelope", Map.of(
								"uid", Urbit.uid(),
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", primaryChatViewTestMessage)
						)
				)
		);

		JsonElement json = gson.toJsonTree(ChatUtils.createMessagePayload("/~zod/test", "~zod", primaryChatViewTestMessage));
//		JsonElement json = gson.toJsonTree(payload);
		CompletableFuture<PokeResponse> pokeFuture = ship.poke(ship.getShipName(), "chat-hook", "json", json);
		await().until(pokeFuture::isDone);
		assertTrue(pokeFuture.get().success);

		// wait until we have at least one proper "chat-update" message that isn't just the initial 20 messages sent
		await().until(
				() -> primaryChatSubscriptionEvents
						.stream()
						.anyMatch(onlyPrimaryChatUpdate)
		);
		primaryChatSubscriptionEvents.stream().
				filter(onlyPrimaryChatUpdate)
				.findFirst()
				.ifPresentOrElse(subscribeEvent -> {
					ChatUpdate chatUpdate = gson.fromJson(subscribeEvent.updateJson.get("chat-update"), ChatUpdate.class);
					System.out.println("Got chat update");
					System.out.println(chatUpdate);
					Objects.requireNonNull(chatUpdate.message);
					assertEquals(primaryChatViewTestMessage, chatUpdate.message.envelope.letter.text);
				}, () -> fail("Chat message received was not the same as the one sent"));

	}


}
