package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.types.Graph;
import airlock.agent.graph.types.Resource;
import airlock.agent.graph.types.content.TextContent;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to test out various functionality of the library and serve as an example
 */
public class Main {

	public static void main(String[] args) throws Exception {

		// SETUP
		// The following example assumes you have:
		// - a ship named 'zod' running
		// - a chat channel called 'test' (you must manually create this)


		// MARK - ship setup
		AirlockCredentials zodCredentials = new AirlockCredentials(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		AirlockChannel channel = new AirlockChannel(zodCredentials);
		channel.authenticate(); // submit the code to the ship for authentication. must be done before anything else
		channel.connect();      // establishes the ServerSideEvent (SSE) client. this is what is used to receive all responses from the ship

		GraphAgent agent = new GraphAgent(channel);
		long NOW = Instant.now().toEpochMilli();
		Resource testGroup = new Resource(channel.getShipName(), "my-own-stuff"); // we are assuming this group already exists

		// 1. create a chat
		Resource chatGraph = new Resource(channel.getShipName(), "test-graph-" + NOW); // we are gonna be creating it so we need a unique name
		agent.createManagedGraph(               // create a managed graph
				chatGraph.name,                 // with the name of the chatGraph
				"Chat made at " + NOW,     // specify title
				"a brand new chat",   // specify description
				testGroup,                      // under the group referenced by the `testGroup` resource
				GraphAgent.Module.CHAT          // with the type of the graph being a chat
		);

		// 2. add a post
		CompletableFuture<PokeResponse> futurePostResponse =
				agent.addPost(
						chatGraph,
						agent.createPost(
								List.of(new TextContent("hey " + Instant.now()))
						)
				);

		assert futurePostResponse.get().success;
		System.out.println(agent.getCurrentGraphs());

		Graph graph = agent.getCurrentGraphs().values().toArray(Graph[]::new)[0];
		System.out.println(graph.firstEntry().getValue().post.contents);

		channel.delete();  // not strictly necessary
		System.out.println("tearing down");
		channel.teardown();

		System.exit(0); // todo for now you need this otherwise it takes like 30 seconds longer to exit

	}
	
}
