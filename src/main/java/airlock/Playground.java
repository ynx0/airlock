package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.types.Resource;
import airlock.agent.graph.types.content.TextContent;
import airlock.types.ShipName;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Playground {

	public static void main(String[] args) throws Exception {


		AirlockCredentials zodCreds = new AirlockCredentials(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		AirlockCredentials sipfynCreds = new AirlockCredentials(new URL("http://localhost:80"), "sipfyn-pidmex", "toprus-dopsul-dozmep-hocbep");
		AirlockChannel urbit = new AirlockChannel(sipfynCreds);
		String ship = urbit.getShipName();
		urbit.authenticate();
		urbit.connect();

		GraphAgent agent = new GraphAgent(urbit);


		long NOW = Instant.now().toEpochMilli();

		Resource testGroupResource = new Resource(ship, "my-own-stuff");
		Resource testGraphResource = new Resource(ship, "test-graph-" + NOW);
		agent.createManagedGraph(
				testGraphResource.name,
				"Title of graph" + NOW,
				"description",
				testGroupResource,
				GraphAgent.Module.CHAT
		);


		CompletableFuture<PokeResponse> futurePostResponse =
				agent.addPost(
						testGraphResource,
						GraphAgent.createPost(
								ship,
								List.of(new TextContent("hey " + Instant.now()))
						)
				);

		assert futurePostResponse.get().success;

		System.out.println("Current graph of the test resource");
		System.out.println(agent.getCurrentGraphs());
		agent.getNewest(testGraphResource, 15);

		urbit.teardown();

	}

}
