import airlock.Urbit;
import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.Resource;
import airlock.errors.AirlockChannelError;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;

import static airlock.AirlockUtils.map2json;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTestsGraphStore {


	private static Urbit urbit;
	private static GraphAgent graphStoreAgent;

	@BeforeAll
	public static void setup() throws MalformedURLException, AirlockChannelError {
		int port = 8080;
		URL url = new URL("http://localhost:" + port);
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		urbit = new Urbit(url, shipName, code);
		urbit.authenticate();
		urbit.connect();
		graphStoreAgent = new GraphAgent(urbit);

		// Assumes fake ship zod is booted and running

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

	@Test
	@Order(1)
	public void createGraph() throws Exception {
		await().until(urbit::isConnected);

		long NOW = Instant.now().toEpochMilli();
		String graphName = "test-graph" + NOW;
		String graphTitle = "Test Graph Created " + NOW;
		String graphDescription = "graph for testing only! having fun strictly prohibited";
		Resource associatedGroup = new Resource("~zod", "TEST_GROUP_" + NOW);
		JsonElement responseJson = graphStoreAgent.createManagedGraph(
				graphName,
				graphTitle,
				graphDescription,
				associatedGroup,
				GraphAgent.Module.LINK
		);

		assertTrue(responseJson.isJsonNull()); // a successful call gives us back a null

		JsonObject keysPayload = graphStoreAgent.getKeys();
		JsonArray keys = keysPayload.get("graph-update").getAsJsonObject().get("keys").getAsJsonArray();
		JsonObject expectedKey = map2json(Map.of(
				"ship", "zod",
				"name", graphName
		));
		// we expect to see a key with our ship and the name of the graph that we just created
		assertTrue(keys.contains(expectedKey));

	}


}
