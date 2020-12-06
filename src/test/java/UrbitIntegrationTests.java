import airlock.InMemoryResponseWrapper;
import airlock.PokeResponse;
import airlock.SubscribeEvent;
import airlock.Urbit;
import airlock.agent.chat.ChatUpdate;
import airlock.agent.chat.ChatUtils;
import com.google.gson.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
	private static Urbit urbit;

	private static CompletableFuture<PokeResponse> futureChatPokeResponse1;
	private static List<SubscribeEvent> subscribeToMailboxEvents;

	private static List<SubscribeEvent> primaryChatSubscriptionEvents;
	private final CompletableFuture<String> futurePrimaryChatMessage = new CompletableFuture<>();
	private final String primaryChatViewTestMessage = "Primary Chat view Test Message" + Instant.now().toEpochMilli();


	Predicate<SubscribeEvent> onlyPrimaryChatUpdate = subscribeEvent ->       // anything that is:
			subscribeEvent.eventType.equals(SubscribeEvent.EventType.UPDATE)  // an update event
					&& subscribeEvent.updateJson.has("chat-update")  // and the update json contains a "chat-update" object
					&& subscribeEvent.updateJson.getAsJsonObject("chat-update").has("message");


	/* TODOs
	 * TODO add tests for subscription canceling
	 * TODO test manually canceling eventsource / deleting channel
	 */


	@BeforeAll
	public static void setup() throws MalformedURLException {
		int port = 8080;
		URL url = new URL("http://localhost:" + port);
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		urbit = new Urbit(url, shipName, code);
		subscribeToMailboxEvents = new ArrayList<>();
		primaryChatSubscriptionEvents = new ArrayList<>();
		gson = new Gson();

		// Assumes fake ship zod is booted and running
		// Assumes chat channel called 'test' is created

	}

	@Test
	@Order(1)
	public void successfulAuthentication() throws ExecutionException, InterruptedException {
		CompletableFuture<String> futureResponseString = new CompletableFuture<>();
		assertDoesNotThrow(() -> {
			InMemoryResponseWrapper res = urbit.authenticate();
			futureResponseString.complete(res.getBody().utf8());
		});
		await().until(futureResponseString::isDone);
		assertEquals("", futureResponseString.get());
	}

	@Test
	@Order(2)
	public void successfullyConnectToShip() {
		await().until(urbit::isAuthenticated);
		assertDoesNotThrow(() -> urbit.connect());
	}


	@Test
	@Order(3)
	public void canSubscribeToTestChat() throws IOException {
		await().until(urbit::isConnected);

		int subscriptionID = urbit.subscribe(urbit.getShipName(), "chat-store", "/mailbox/~zod/test", subscribeEvent -> {
			subscribeToMailboxEvents.add(subscribeEvent);
		});

		await().until(() -> subscribeToMailboxEvents.size() >= 2);
		assertEquals(SubscribeEvent.EventType.STARTED, subscribeToMailboxEvents.get(0).eventType);
		// todo add assertion for the second event
	}


	@Test
	@Order(4)
	public void canSendChatMessage() throws IOException, ExecutionException, InterruptedException {
		await().until(urbit::isConnected);
		await().until(() -> !subscribeToMailboxEvents.isEmpty());

		Map<String, Object> payload = Map.of(
				"message", Map.of(
						"path", "/~zod/test",
						"envelope", Map.of(
								"uid", Urbit.uid(),
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", "Hello, Mars! It is now " + Instant.now().toString())
						)
				)
		);

		futureChatPokeResponse1 = urbit.poke(urbit.getShipName(), "chat-hook", "json", gson.toJsonTree(payload));
		await().until(futureChatPokeResponse1::isDone);

		assertTrue(futureChatPokeResponse1.get().success);
	}

	@Test
	@Order(5)
	public void testChatView() throws IOException, ExecutionException, InterruptedException {
		await().until(urbit::isConnected);
		await().until(futureChatPokeResponse1::isDone);


		int subscriptionID = urbit.subscribe(urbit.getShipName(), "chat-view", "/primary", subscribeEvent -> {
			primaryChatSubscriptionEvents.add(subscribeEvent);
		});

		// send a message to a chat that we haven't subscribed to already
		// todo reimpl above behavior. it will fail on ci because integration test setup does not create it


		// the specification of this payload is at lib/chat-store.hoon#L119...

		JsonElement json = gson.toJsonTree(ChatUtils.createMessagePayload("/~zod/test", "~zod", primaryChatViewTestMessage));
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
					ChatUpdate chatUpdate = gson.fromJson(subscribeEvent.updateJson.get("chat-update"), ChatUpdate.class);
					System.out.println("Got chat update");
					System.out.println(chatUpdate);
					Objects.requireNonNull(chatUpdate.message);
					assertEquals(primaryChatViewTestMessage, chatUpdate.message.envelope.letter.text);
					futurePrimaryChatMessage.complete(chatUpdate.message.envelope.letter.text);
				}, () -> fail("Chat message received was not the same as the one sent"));

	}

	@Test
	@Order(6)
	public void canScry() throws IOException {
		await().until(urbit::isConnected);
		JsonElement responseJson = urbit.scryRequest("file-server", "/clay/base/hash");
		assertEquals(responseJson.getAsInt(), 0);
	}

	@Test
	@Order(7)
	public void scryGraph() throws IOException {
		await().until(urbit::isConnected);
		JsonElement keyScry = urbit.scryRequest("graph-store", "/keys");
		JsonElement tagScry = urbit.scryRequest("graph-store", "/tags");
		JsonElement tagQueriesScry = urbit.scryRequest("graph-store", "/tag-queries");
		System.out.println("graph scry: /keys response");
		System.out.println(keyScry);

		System.out.println("graph scry: /tags response");
		System.out.println(tagScry);


		System.out.println("graph scry: /tag-queries response");
		System.out.println(tagQueriesScry);
	}


	@Test
	@Order(8)
	public void canSpider() throws IOException {
		await().until(urbit::isConnected);

		//  this is taken directly from https://urbit.org/using/integrating-api/, but doesn't work in its current state
		//  todo maybe make a pull request and put an actual working example in that doc
		// todo improve this test to verify the creation process better
		long NOW = Instant.now().toEpochMilli();
		JsonObject graphPayload = gson.toJsonTree(Map.of(
				// https://github.com/urbit/urbit/blob/531f406222c15116c2ff4ccc6622f1eae4f2128f/pkg/interface/src/views/landscape/components/NewChannel.tsx#L98
				"create", Map.of(
						"resource", Map.of(
								"ship", "~zod",       // =entity
								"name", "test-graph" + NOW    // name=term
						),
						"title", "Test Graph!!!" + NOW,
						"description", "graph for testing only! having fun strictly prohibited",
						"associated", Map.of(
								"group", Map.of(
										"ship", "~zod",
										"name", "TEST_GROUP" + NOW
								)
						),
						"module", "link"
				)
		)).getAsJsonObject();
		InMemoryResponseWrapper responseWrapper = urbit.spiderRequest("graph-view-action", "graph-create", "json", graphPayload.getAsJsonObject());
		assertTrue(responseWrapper.getClosedResponse().isSuccessful());
		assertTrue(JsonParser.parseString(responseWrapper.getBody().utf8()).isJsonNull());

	}


}
