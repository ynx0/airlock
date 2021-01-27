package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.types.*;
import airlock.agent.graph.types.content.TextContent;
import airlock.agent.graph.types.content.UrlContent;
import airlock.apps.publish.Publisher;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("DuplicatedCode")
public class Playground {

	public static void main(String[] args) throws Exception {


		AirlockCredentials zodCreds = new AirlockCredentials(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		AirlockCredentials sipfynCreds = new AirlockCredentials(new URL("http://localhost:80"), "sipfyn-pidmex", "toprus-dopsul-dozmep-hocbep");
		AirlockChannel channel = new AirlockChannel(sipfynCreds);
		String ship = channel.getShipName();
		channel.authenticate();
		channel.connect();

		GraphAgent agent = new GraphAgent(channel);
		Publisher publisher = new Publisher(channel);


		long NOW = AirlockUtils.currentTimeMS();
		Resource testGroup = new Resource(ship, "my-own-stuff"); // we are assuming this group already exists

		// todo: landscape subscribes to `/all` on graph-store so it may be getting back messages
		//  which we may not because we haven't subscribed to `/all` nor any single graph update I don't think

		// todo: for unit tests, add more rigorous assertions. right now we only (soft-)"assert" for success
		//  but in the actual unit tests we should ensure other invariants such as the fact that the right message went to the right channel etc.


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
						agent.createPost(
								List.of(new TextContent("hey " + Instant.now()))
						)
				);

		assert futurePostResponse.get().success;
		System.out.println(agent.getCurrentGraphs());

		// todo whats the diff between get newest vs getYounger/Older
		// entrypoint: https://github.com/urbit/urbit/blob/6499eb5fe0bd81c91f12d6f9ebcc6843b2ca7ac7/pkg/interface/src/views/apps/chat/components/ChatWindow.tsx#L200
		// the above line of code has the landscape logic for fetching messages
		// ChatResource wraps ChatWindow and ChatInput.
		// ChatWindow calls `getOlder` when you are scrolling up and need older entries, and `getYounger` when you are scrolling down and need newer entries.
		// Only `ChatResource` calls `api.getNewest` (directly).

		// 3. get newest content
		agent.getNewest(chatGraph, 15);




		// MARK - Links/Collections
		// 1. create a new collection
		Resource linksGraph = new Resource(ship, "test-graph-" + NOW); // we are gonna be creating it so we need a unique name
		agent.createManagedGraph(                            // create a managed graph
				linksGraph.name,                             // with the name of the chatGraph
				"Link Collection made at " + NOW,       // specify title
				"a brand new collection",          // specify description
				testGroup,                                   // under the group referenced by the `testGroup` resource
				GraphAgent.Module.LINK                       // with the type of the graph being a chat
		);


		// N.B: although the `contents` variable is generic enough to be a list of any contents,
		// the links module expects the contents to be a list with the following structure
		// Link = List(Title, Url). that is, a length-two list with the first element being a `textcontent` representing the title
		// and the second element being a url content representing the link to associate with the title
		// todo test to see what happens when we mix content or send stuff not with the exact schema

		// 2. add a link to the collection
		// a. create a link
		// $CODE_TO_CREATE_LINK

		// b. add link post
		CompletableFuture<PokeResponse> addLinkResponse =
				agent.addPost(
						linksGraph,
						agent.createPost(
								List.of(
										new TextContent("Title of my link"),
										new UrlContent("https://urbit.org")
								)
						)
				);

		System.out.println(agent.getCurrentGraphs());
		assert addLinkResponse.get().success;


		// 3. update link

		// 4. post comment

		// 5. update comment

		// 6. delete comment

		// 7. delete link


		// MARK - Publish

		// 1. create new notebook
		Resource notebookGraph = new Resource(ship, "test-graph-" + NOW); // we are gonna be creating it so we need a unique name
		agent.createManagedGraph(                            // create a managed graph
				notebookGraph.name,                          // with the name of the chatGraph
				"Notebook made at " + NOW,              // specify title
				"a brand new notebook",            // specify description
				testGroup,                                   // under the group referenced by the `testGroup` resource
				GraphAgent.Module.PUBLISH                    // with the type of the graph being a chat
		);


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

		// 2. publish to the notebook

		// a. create post
		long timeSent = AirlockUtils.currentTimeMS();
		NodeMap newPublishPost = publisher.newPost("Title of My Blog Post", "Body Content", timeSent);

		// b. add notebook post
		CompletableFuture<PokeResponse> notebookPostResponse =
				agent.addNodes(
						notebookGraph,
						newPublishPost
				);
		assert notebookPostResponse.get().success;

		// 3. update post
		// adapted from https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/apps/publish/components/EditPost.tsx#L36
		Node blogPostRoot = newPublishPost.values().iterator().next();  // get first (and only) node of our node map which corresponds tho the root blog
		BigInteger blogPostId = newPublishPost.keySet().iterator().next().get(0); // get the index of the blog root, then get the id which is the 0th element of the index
		BigInteger latestRevisionNum = publisher.getLatestRevisionNum(blogPostRoot);
		Node latestRevision = publisher.getLatestRevision(blogPostRoot);

		var newRevision = latestRevisionNum.add(BigInteger.ONE);
		var updatedNodes = publisher.editPost(newRevision, blogPostId, "New Title", "New Body", NOW);
		agent.addNodes(notebookGraph, updatedNodes);

		// todo check that latest is post revision is now ours


		// 4. add a comment
		// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/components/Comments.tsx#L33
		// for now, we will not be handling mentions



		// 5. update comment
		// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/components/Comments.tsx#L53


		// 6. delete comment
		// https://github.com/urbit/urbit/blob/03eb12698a5ccb9675dd3e638c89bfc78266a22b/pkg/interface/src/views/components/CommentItem.tsx#L39


		// 7. delete post
			// so CommentItem, apps/link/LinkItem, publish/Note, all use removeNodes, which means deleting them is well defined for landscape
			// however, what about chat? todo experiment
		// https://github.com/urbit/urbit/blob/2d6913794f611093feefc8895294dcb2d07ec7a0/pkg/interface/src/views/apps/publish/components/Note.tsx#L38




		// MARK - All Graphs
		// 1. get older/younger siblings
		// agent.getOlderSiblings(chatGraph, 15, String.valueOf(Instant.ofEpochMilli(1234444234))); // this is untested
		// agent.getYoungerSiblings(chatGraph, 15, String.valueOf(Instant.ofEpochMilli(1234444234))); // this is untested


		// 2. Delete graphs
		JsonElement deleteChatResponse = agent.deleteGraph(chatGraph.name);
		assert deleteChatResponse.equals(JsonNull.INSTANCE); // returns `null` on success

		JsonElement deleteLinksResponse = agent.deleteGraph(linksGraph.name);
		assert deleteLinksResponse.equals(JsonNull.INSTANCE);

		JsonElement deletePublishResponse = agent.deleteGraph(linksGraph.name);
		assert deletePublishResponse.equals(JsonNull.INSTANCE);


		// MARK - tear down urbit instance
		channel.teardown();

	}

}
