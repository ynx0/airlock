package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.types.Resource;
import airlock.agent.graph.types.content.TextContent;
import airlock.agent.graph.types.content.UrlContent;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("DuplicatedCode" )
public class Playground {

	public static void main(String[] args) throws Exception {


		AirlockCredentials zodCreds = new AirlockCredentials(new URL("http://localhost:8080" ), "zod", "lidlut-tabwed-pillex-ridrup" );
		AirlockCredentials sipfynCreds = new AirlockCredentials(new URL("http://localhost:80" ), "sipfyn-pidmex", "toprus-dopsul-dozmep-hocbep" );
		AirlockChannel urbit = new AirlockChannel(sipfynCreds);
		String ship = urbit.getShipName();
		urbit.authenticate();
		urbit.connect();

		GraphAgent agent = new GraphAgent(urbit);


		long NOW = Instant.now().toEpochMilli();
		Resource testGroup = new Resource(ship, "my-own-stuff" ); // we are assuming this has been created for us




		// MARK - Chat
		// 1. create a chat
		Resource chatGraph = new Resource(ship, "test-graph-" + NOW); // we are gonna be creating it so we need a unique name
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
						GraphAgent.createPost(
								ship,
								List.of(new TextContent("hey " + Instant.now()))
						)
				);

		assert futurePostResponse.get().success;
		System.out.println(agent.getCurrentGraphs());

		// 3. get latest content
		agent.getNewest(chatGraph, 15);
//		agent.getOlderSiblings(chatGraph, 15, String.valueOf(Instant.ofEpochMilli(1234444234))); // this is untested
//		agent.getYoungerSiblings(chatGraph, 15, String.valueOf(Instant.ofEpochMilli(1234444234))); // this is untested




		// MARK - Links/Collections
	/*	Resource linksGraph = new Resource(ship, "test-graph-" + NOW); // we are gonna be creating it so we need a unique name
		agent.createManagedGraph(                            // create a managed graph
				linksGraph.name,                             // with the name of the chatGraph
				"Link Collection made at " + NOW,       // specify title
				"a brand new collection",          // specify description
				testGroup,                                   // under the group referenced by the `testGroup` resource
				GraphAgent.Module.LINK                       // with the type of the graph being a chat
		);


		// 2. add a link to the collection
		// N.B: although the `contents` variable is generic enough to be a list of any contents,
		// the links module expects the contents to be a list with the following structure
		// Link = List(Title, Url). that is, a length-two list with the first element being a textcontent representing the title
		// and the second element being a url content representing the link to associate with the title
		// todo test to see what happens when we mix content or send stuff not with the exact schema
		CompletableFuture<PokeResponse> addLinkResponse =
				agent.addPost(
						linksGraph,
						GraphAgent.createPost(
								ship,
								List.of(
										new TextContent("Title of my link"),
										new UrlContent("https://urbit.org")
								)
						)
				);

		assert addLinkResponse.get().success;
		System.out.println(agent.getCurrentGraphs());

		// 3. get latest content
		agent.getNewest(linksGraph, 15);

		// no need to repeat the others*/




		// MARK - Links/Collections
		Resource notebookGraph = new Resource(ship, "test-graph-" + NOW); // we are gonna be creating it so we need a unique name
		agent.createManagedGraph(                            // create a managed graph
				notebookGraph.name,                          // with the name of the chatGraph
				"Notebook made at " + NOW,       // specify title
				"a brand new notebook",          // specify description
				testGroup,                                   // under the group referenced by the `testGroup` resource
				GraphAgent.Module.PUBLISH                    // with the type of the graph being a chat
		);


		// 2. publish to the notebook
		// N.B: the structure of the initial payload sent to publish module is non obvious.
		// it does not start off as a single node with no children and a post.
		// The structure is like so:
		// (note the double-nestedness of each element)
		/*
		Graph {
			// wrapper around the whole thing: post and associated comments
			Node(/170) {
				post: empty post
				children: {
					// first child stores the post
					Node(/1) {
						post: empty post
						children: {
							Node(/1) {
								post: contents: {
									{"text": "This is interpreted as the title"},
									{"text": "This is interpreted as the body"}
								}
								children: null
							}
						}
					}
					// second child stores the comments
					Node(/2) {
						post: empty post
						children: {
							Node(/171) {
								post: empty post
								children: {
									Node(/1) {
										post: contents: "a comment",
										children: null
									}
								}
							}
						}
					}
				}
			}
		}

		 */

		CompletableFuture<PokeResponse> addLinkResponse =
				agent.addPost(
						notebookGraph,
						GraphAgent.createPost(
								ship,
								List.of(
										new TextContent("Title of my link"),
										new UrlContent("https://urbit.org")
								)
						)
				);

		assert addLinkResponse.get().success;
		System.out.println(agent.getCurrentGraphs());

		// 3. get latest content
		agent.getNewest(notebookGraph, 15);

		// no need to repeat the others

		// MARK - tear down
		urbit.teardown();

	}

}
