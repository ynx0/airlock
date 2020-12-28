import airlock.AirlockChannel;
import airlock.PokeResponse;
import airlock.agent.graph.*;
import airlock.agent.graph.types.Resource;
import airlock.agent.graph.types.content.TextContent;
import airlock.errors.channel.AirlockChannelError;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static airlock.AirlockUtils.map2json;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTestsGraphStore {


	private static AirlockChannel urbit;
	private static GraphAgent graphStoreAgent;
	public static final long NOW = Instant.now().toEpochMilli();
	public static final String GRAPH_NAME = "test-chat-" + NOW;
	public static final String GRAPH_TITLE = "Test Graph Created " + NOW;
	public static final String GRAPH_DESCRIPTION = "graph for testing only! having fun strictly prohibited";
	public static final Resource ASSOCIATED_GROUP = new Resource("~zod", "test-group");

	@BeforeAll
	public static void setup() throws MalformedURLException, AirlockChannelError {
		int port = 8080;
		URL url = new URL("http://localhost:" + port);
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		urbit = new AirlockChannel(url, shipName, code);
		urbit.authenticate();
		urbit.connect();
		graphStoreAgent = new GraphAgent(urbit);

		// Assumes fake ship zod is booted and running, and group exists named `test-group`

	}

	@AfterAll
	public static void teardown() {
		urbit.teardown();
	}

	@Test
	@Order(1)
	public void canCreateGraph() throws Exception {
		await().until(urbit::isConnected);
		// todo create the group
		JsonElement responseJson = graphStoreAgent.createManagedGraph(
				GRAPH_NAME,
				GRAPH_TITLE,
				GRAPH_DESCRIPTION,
				ASSOCIATED_GROUP,
				GraphAgent.Module.CHAT
		);

		assertTrue(responseJson.isJsonNull()); // a successful call gives us back a null

		JsonObject keysPayload = graphStoreAgent.getKeys();
		JsonArray keys = keysPayload.get("graph-update").getAsJsonObject().get("keys").getAsJsonArray();
		JsonObject expectedKey = map2json(Map.of(
				"ship", "zod",
				"name", GRAPH_NAME
		));
		// we expect to see a key with our ship and the name of the graph that we just created
		assertTrue(keys.contains(expectedKey));

	}

	@Test
	@Order(2)
	@Disabled("As of v1.0-rc1, the fakezod creation process is broken, causing this test to fail")
	public void canSendChatOnGraph() throws Exception {
		String shipName = urbit.getShipName();
		CompletableFuture<PokeResponse> futureResponse = graphStoreAgent.addPost(
				shipName,
				GRAPH_NAME,
				GraphAgent.createPost(
						shipName,
						Collections.singletonList(new TextContent("hello world")),
						null,
						null
				)
		);
		PokeResponse response = futureResponse.get();

		assertTrue(response.success);

	}

	/* TODOs
	// todo make test that gets all of these
			JsonObject tagScry = urbit.scryRequest("graph-store", "/tags").getAsJsonObject();
		JsonObject tagQueriesScry = urbit.scryRequest("graph-store", "/tag-queries").getAsJsonObject();

		System.out.println("graph scry: /tags response");
		System.out.println(tagScry);
		assertTrue(tagScry.has("graph-update"));
		assertTrue(tagScry.get("graph-update").getAsJsonObject().has("tags"));


		System.out.println("graph scry: /tag-queries response");
		System.out.println(tagQueriesScry);
		assertTrue(tagQueriesScry.has("graph-update"));
		assertTrue(tagQueriesScry.get("graph-update").getAsJsonObject().has("tag-queries"));
	 */

	/*
	// todo potentially do something with this code

//		JsonArray graphKeys = agent.getKeys().getAsJsonObject()
//				.get("graph-update").getAsJsonObject()
//				.get("keys").getAsJsonArray();
//		JsonObject graphOwnedByMe = StreamSupport.stream(graphKeys.spliterator(), false)
//				.map(JsonElement::getAsJsonObject)
//				.filter(key -> key.get("ship").getAsString().equals("sipfyn-pidmex"))
//				.filter(key -> !key.get("name").getAsString().contains("dm--"))
//				.filter(key -> key.get("name").getAsString().contains("2"))
//				.findFirst().orElseThrow().getAsJsonObject();
//
//		System.out.println(graphKeys);
//		System.out.println(graphOwnedByMe);
//		JsonElement graphUpdate = agent.getGraph(ShipName.withSig(graphOwnedByMe.get("ship").getAsString()),graphOwnedByMe.get("name").getAsString());
//		System.out.println(AirlockUtils.gson.toJson(graphUpdate));
	 */


}
