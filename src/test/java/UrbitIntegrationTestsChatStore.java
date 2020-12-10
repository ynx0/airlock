import airlock.AirlockUtils;
import airlock.PokeResponse;
import airlock.SubscribeEvent;
import airlock.Urbit;
import airlock.agent.chat.ChatUpdate;
import airlock.agent.chat.ChatUtils;
import airlock.agent.chat.MessagePayload;
import airlock.errors.AirlockChannelError;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTestsChatStore {
	private static Urbit urbit;

	private static CompletableFuture<PokeResponse> futureChatPokeResponse;
	private static List<SubscribeEvent> subscribeToMailboxEvents;

	private static List<SubscribeEvent> primaryChatSubscriptionEvents;
	private final CompletableFuture<String> futurePrimaryChatMessage = new CompletableFuture<>();
	private final String primaryChatViewTestMessage = "Primary Chat view Test Message" + Instant.now().toEpochMilli();


	final Predicate<SubscribeEvent> onlyPrimaryChatUpdate = subscribeEvent ->       // anything that is:
			subscribeEvent.eventType.equals(SubscribeEvent.EventType.UPDATE)  // an update event
					&& subscribeEvent.updateJson.has("chat-update")  // and the update json contains a "chat-update" object
					&& subscribeEvent.updateJson.getAsJsonObject("chat-update").has("message");


	/* TODOs
	 * TODO add tests for subscription canceling
	 * TODO test manually canceling eventsource / deleting channel
	 */


	@BeforeAll
	public static void setup() throws MalformedURLException, AirlockChannelError {
		int port = 8080;
		URL url = new URL("http://localhost:" + port);
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		urbit = new Urbit(url, shipName, code);
		urbit.authenticate();
		urbit.connect();

		subscribeToMailboxEvents = new ArrayList<>();
		primaryChatSubscriptionEvents = new ArrayList<>();


		// Assumes fake ship zod is booted and running
		// Assumes chat channel called 'test' is created

	}


	@Test
	@Order(1)
	public void canSubscribeToTestChat() throws Exception {
		await().until(urbit::isConnected);

		int subscriptionID = urbit.subscribe(urbit.getShipName(), "chat-store", "/mailbox/~zod/test", subscribeEvent -> subscribeToMailboxEvents.add(subscribeEvent));

		await().until(() -> subscribeToMailboxEvents.size() >= 2);
		assertEquals(SubscribeEvent.EventType.STARTED, subscribeToMailboxEvents.get(0).eventType);
		// todo add assertion for the second event
	}


	@Test
	@Order(2)
	public void canSendChatMessage() throws Exception {
		await().until(urbit::isConnected);
		await().until(() -> !subscribeToMailboxEvents.isEmpty());

		String path = "/~zod/test";
		String author = "~zod";
		String textContent = "Hello, Mars! It is now " + Instant.now().toString();
		MessagePayload payload = ChatUtils.createMessagePayload(path, author, textContent);

		futureChatPokeResponse = urbit.poke(urbit.getShipName(), "chat-hook", "json", AirlockUtils.gson.toJsonTree(payload));
		await().until(futureChatPokeResponse::isDone);

		assertTrue(futureChatPokeResponse.get().success);
	}

	@Test
	@Order(3)
	public void testChatView() throws Exception {
		await().until(urbit::isConnected);
		await().until(futureChatPokeResponse::isDone);


		int subscriptionID = urbit.subscribe(urbit.getShipName(), "chat-view", "/primary", subscribeEvent -> primaryChatSubscriptionEvents.add(subscribeEvent));

		// send a message to a chat that we haven't subscribed to already
		// todo re implement above behavior. it will fail on ci because integration test setup does not create it


		// the specification of this payload is at lib/chat-store.hoon#L119...

		JsonElement json = AirlockUtils.gson.toJsonTree(ChatUtils.createMessagePayload("/~zod/test", "~zod", primaryChatViewTestMessage));
		CompletableFuture<PokeResponse> pokeFuture = urbit.poke(urbit.getShipName(), "chat-hook", "json", json);
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
					ChatUpdate chatUpdate = AirlockUtils.gson.fromJson(subscribeEvent.updateJson.get("chat-update"), ChatUpdate.class);
					System.out.println("Got chat update");
					System.out.println(chatUpdate);
					Objects.requireNonNull(chatUpdate.message);
					String messageText = chatUpdate.message.envelope.letter.text;
					assertEquals(primaryChatViewTestMessage, messageText);
					futurePrimaryChatMessage.complete(messageText);
				}, () -> fail("Chat message received was not the same as the one sent"));

	}
}
