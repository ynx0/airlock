package airlock.agent.graph;

import airlock.AirlockChannel;
import airlock.AirlockUtils;
import airlock.PokeResponse;
import airlock.agent.Agent;
import airlock.agent.graph.types.*;
import airlock.agent.graph.types.content.GraphContent;
import airlock.agent.group.GroupUtils;
import airlock.agent.group.types.GroupPolicy;
import airlock.errors.channel.AirlockAuthenticationError;
import airlock.errors.channel.AirlockRequestError;
import airlock.errors.channel.AirlockResponseError;
import airlock.errors.scry.ScryDataNotFoundException;
import airlock.errors.scry.ScryFailureException;
import airlock.errors.spider.SpiderFailureException;
import airlock.types.ShipName;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static airlock.AirlockUtils.gson;
import static airlock.AirlockUtils.map2json;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.StreamSupport.stream;

/**
 * This class represents the client side implementation of the %graph-store api,
 * and allows you to interact with the %graph-store gall agent that is running on a ship through a given channel.
 *
 * It does minimal state handling; it keeps track of the latest keys and graphs
 * based on the <code>graph-update</code> payload it receives,
 * but does not subscribe to "/all" by default.
 */
// todo landscape is moving away from subscribing to "/all" for some agents
//  we should watch for what they do instead and see if we want to implement it
public class GraphAgent extends Agent {


	/**
	 * Resources of all known graphs
	 */
	private final Set<Resource> keys;

	/**
	 * Map of all known graphs keyed by Resource
	 */
	private final Map<Resource, Graph> graphs;

	/**
	 * Create a GraphAgent on a given channel
	 * @param channel the channel to create the agent on
	 */
	public GraphAgent(AirlockChannel channel) {
		super(channel);
		this.keys = new HashSet<>();
		this.graphs = new HashMap<>();
		// adapting from new landscape api https://github.com/urbit/urbit/blob/1895e807fdccd669dd0b514dff1c07aa3bfe7449/pkg/interface/src/logic/api/graph.ts
		// and also https://github.com/urbit/urbit/blob/51fd47e886092a842341df9da549f77442c56866/pkg/interface/src/types/graph-update.ts
		// https://github.com/urbit/urbit/blob/master/pkg/interface/src/logic/reducers/graph-update.js
		// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/store/store.ts
		// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/api/graph.ts
		// todo custom dataclass for graph-update with all derivatives
	}


	// Note: most of the following code has been ported directly from channel.js
	// As such, there is only so much commentary I am able to provide about the underlying design and logic of the code

	/*

	export const createBlankNodeWithChildPost = (
	  parentIndex: string = '',
	  childIndex: string = '',
	  contents: Content[]
	) => {
	  const date = unixToDa(Date.now()).toString();
	  const nodeIndex = parentIndex + '/' + date;

	  const childGraph = {};
	  childGraph[childIndex] = {
		post: {
		  author: `~${window.ship}`,
		  index: nodeIndex + '/' + childIndex,
		  'time-sent': Date.now(),
		  contents,
		  hash: null,
		  signatures: []
		},
		children: null
	  };

	  return {
		post: {
		  author: `~${window.ship}`,
		  index: nodeIndex,
		  'time-sent': Date.now(),
		  contents: [],
		  hash: null,
		  signatures: []
		},
		children: childGraph
	  };
	};
		 */
	// thought: the original api calls Date.now() multiple times instead of using a single value. is this behavior preferred or necessary?
	// todo move this to node? it could make more sense there
	// todo make this a set of overloaded methods instead of using `requireNonNullElse`
	/*

		export const createBlankNodeWithChildPost = (
		  parentIndex: string = '',
		  childIndex: string = '',
		  contents: Content[]
		) => {
		  const date = unixToDa(Date.now()).toString();
		  const nodeIndex = parentIndex + '/' + date;

		  const childGraph = {};
		  childGraph[childIndex] = {
			post: {
			  author: `~${window.ship}`,
			  index: nodeIndex + '/' + childIndex,
			  'time-sent': Date.now(),
			  contents,
			  hash: null,
			  signatures: []
			},
			children: null
		  };

		  return {
			post: {
			  author: `~${window.ship}`,
			  index: nodeIndex,
			  'time-sent': Date.now(),
			  contents: [],
			  hash: null,
			  signatures: []
			},
			children: childGraph
		  };
		};
			 */
	public static Node createBlankNodeWithChildPost(String shipAuthor, List<GraphContent> contents, @Nullable Index parentIndex, @Nullable Index childIndex) {
		parentIndex = requireNonNullElse(parentIndex, Index.createEmptyIndex());
		childIndex = requireNonNullElse(childIndex, Index.createEmptyIndex());

		final var date = AirlockUtils.unixToDa(Instant.now().toEpochMilli());
		final var nodeIndex = Index.fromIndex(parentIndex, date);

		Index parsedIndexArray = new Index(childIndex);
		if (parsedIndexArray.size() != 1) {
			// see if we want to keep this or not
			throw new IllegalArgumentException("invalid index provided with size != 1");
		}
		BigInteger index = parsedIndexArray.get(0);

		Graph childGraph = new Graph(Map.of(index, new Node(
				new Post(
						ShipName.withSig(shipAuthor),
						Index.fromIndex(nodeIndex, childIndex),
						Instant.now().toEpochMilli(),
						contents,
						null,
						Collections.emptyList()
				),
				new Graph()
		)));

		return new Node(
				new Post(
						ShipName.withSig(shipAuthor),
						nodeIndex,
						Instant.now().toEpochMilli(),
						Collections.emptyList(),
						null,
						Collections.emptyList()
				),
				childGraph
		);
	}


	public static void markPending(List<Node> nodes) {
		for (Node node : nodes) {
			node.markPending();
		}
	}


/*

export const createPost = (
  contents: Content[],
  parentIndex: string = '',
  childIndex:string = 'DATE_PLACEHOLDER'
) => {
  if (childIndex === 'DATE_PLACEHOLDER') {
    childIndex = unixToDa(Date.now()).toString();
  }
  return {
    author: `~${window.ship}`,
    index: parentIndex + '/' + childIndex,
    'time-sent': Date.now(),
    contents,
    hash: null,
    signatures: []
  };
};
*/

	/**
	 * Create a post with the given author, contents, and indices.
	 * @param shipAuthor
	 * @param contents
	 * @param parentIndex
	 * @param childIndex
	 * @return The newly created post
	 */
	public static Post createPost(String shipAuthor, List<GraphContent> contents, @Nullable Index parentIndex, @Nullable Index childIndex) {
		// todo make this api design more idiomatic by using alternative to requireNonNull api
		// todo move this into the `Post` class instead??
		parentIndex = requireNonNullElse(parentIndex, new Index());

		if (childIndex == null) {
			childIndex = new Index(AirlockUtils.unixToDa(Instant.now().toEpochMilli()));
		}

		return new Post(
				shipAuthor,
				Index.fromIndex(parentIndex, childIndex),
				Instant.now().toEpochMilli(),
				contents,
				null,
				Collections.emptyList()
		);

	}

	public static Post createPost(String shipAuthor, List<GraphContent> contents) {
		return GraphAgent.createPost(shipAuthor, contents, null, null);
	}



	/*
	function moduleToMark(mod: string): string | undefined {
	  if(mod === 'link') {
		return 'graph-validator-link';
	  }
	  if(mod === 'publish') {
		return 'graph-validator-publish';
	  }
	  if(mod === 'chat') {
		return 'graph-validator-chat';
	  }
	  return undefined;
	}
	*/

	/**
	 * This enum describes the possible graph types that can exist. It forms a mapping between a module/app and the corresponding mark.
	 */
	public enum Module {
		LINK("graph-validator-link"),
		PUBLISH("graph-validator-publish"),
		CHAT("graph-validator-chat");

		public final String mark;

		Module(String mark) {
			this.mark = mark;
		}

		public String moduleName() {
			return this.name().toLowerCase();
		}

	}

	static String moduleToMark(Module module) {
		return module.mark;
	}

	/**
	 * Perform a `store` action with the provided payload. This method is used to send a `graph-update` to the ship.
	 * @param payload The payload to send
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	private CompletableFuture<PokeResponse> storeAction(JsonObject payload) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		return this.action("graph-store", "graph-update", payload, null);
	}

	/**
	 * Perform a view action. This method is used to get data from a graph in some way by means of a spider (one-off thread).
	 * @param threadName
	 * @param payload
	 * @return Success/fail response to our request. (JsonNull on sucess).
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws SpiderFailureException
	 * @throws AirlockAuthenticationError
	 */
	private JsonElement viewAction(String threadName, JsonObject payload) throws AirlockResponseError, AirlockRequestError, SpiderFailureException, AirlockAuthenticationError {
		return this.urbit.spiderRequest("graph-view-action", threadName, "json", payload);
	}

	/*
  private hookAction(ship: Patp, action: any): Promise<any> {
    return this.action('graph-push-hook', 'graph-update', action);
  }

*/

	/**
	 * Perform a hook action.
	 * @param ship
	 * @param payload
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	private CompletableFuture<PokeResponse> hookAction(String ship, JsonObject payload) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		// okay i don't know if you actually need ship or not based simply on porting right now
		// so i guess todo to test out ship unused parameter behaviour
		return this.action("graph-push-hook", "graph-update", payload, ship);
	}


	/*
  createManagedGraph(
    name: string,
    title: string,
    description: string,
    group: Path,
    mod: string
  ) {
    const associated = { group: resourceFromPath(group) };
    const resource = makeResource(`~${window.ship}`, name);

    return this.viewAction('graph-create', {
      "create": {
        resource,
        title,
        description,
        associated,
        "module": mod,
        mark: moduleToMark(mod)
      }
    });
  }
  */
	public JsonElement createManagedGraph(String name, String title, String description, String pathOfGroup, Module module) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.createManagedGraph(name, title, description, GroupUtils.resourceFromPath(pathOfGroup), module);
	}

	/**
	 * Creates a managed graph (a graph nested within a group) with the provided details.
	 * @param name The name of the graph to create
	 * @param title The (human readable) title of the graph
	 * @param description The description of the graph
	 * @param groupResource The group to create the graph under
	 * @param module The desired module of the graph
	 * @return Success/fail response to our request. (JsonNull on sucess).
	 * @throws SpiderFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement createManagedGraph(String name, String title, String description, Resource groupResource, Module module) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		// todo note: name should be url safe. also, landscape tends to replace spaces and other characters with dashes, but we aren't doing that yet
		final var associated = Map.of("group", groupResource);
		final var resource = GroupUtils.makeResource(ShipName.withSig(this.urbit.getShipName()), name);

		JsonObject createPayload = gson.toJsonTree(Map.of(
				"create", Map.of(
						"resource", resource,
						"title", title,
						"description", description,
						"associated", associated,
						"module", module.moduleName(),
						"mark", moduleToMark(module)
				)
		)).getAsJsonObject();

		return this.viewAction("graph-create", createPayload);
	}

	/*
  createUnmanagedGraph(
    name: string,
    title: string,
    description: string,
    policy: Enc<GroupPolicy>,
    mod: string
  ) {
    const resource = makeResource(`~${window.ship}`, name);

    return this.viewAction('graph-create', {
      "create": {
        resource,
        title,
        description,
        associated: { policy },
        "module": mod,
        mark: moduleToMark(mod)
      }
    });
  }
*/


	public JsonElement createUnmanagedGraph(String name, String title, String description, GroupPolicy policy, Module module) throws AirlockResponseError, AirlockRequestError, SpiderFailureException, AirlockAuthenticationError {
		/*
		This requires Enc<S>, which idk wtf it's doing. I mean i guess i get it now but like translating this requires a different way of doing things compeltely.
		I think i can just implement the equivalent in a custom seralizer or a utlitiy class...

		// Turns sets into arrays and maps into objects so we can send them over the wire
		export type Enc<S> =
		S extends Set<any> ?
		Enc<SetElement<S>>[] :
		S extends Map<string, any> ?
		{ [s: string]: Enc<MapValue<S>> } :
		S extends object ?
				{ [K in keyof S]: Enc<S[K]> } :
		S;

		 */
		final var resource = GroupUtils.makeResource(urbit.getShipName(), name);
		return this.viewAction("graph-create", map2json(Map.of(
				"create", Map.of(
						"resource", resource,
						"title", title,
						"description", description,
						"associated", Map.of("policy", policy),
						"module", module.moduleName(),
						"mark", moduleToMark(module)
				)
		)));

	}

	/**
	 * Tell our ship to join a graph specified by the resource.
	 * @param resource The resource referring to the graph we want to join
	 * @return Success/fail response to our request. (JsonNull on sucess).
	 * @throws SpiderFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement joinGraph(Resource resource) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.viewAction("graph-join", map2json(Map.of(
				"join", Map.of(
						"resource", resource,
						"ship", resource.ship
				)
		)));
	}


	/**
	 * Delete a graph. With this method, it is only possible to delete a graph which is a resource we own (under our ship).
	 * @param name The name of the graph to delete
	 * @return Success/fail response to our request. (JsonNull on sucess).
	 * @throws SpiderFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement deleteGraph(String name) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		final var resource = GroupUtils.makeResource(this.urbit.getShipName(), name);
		return this.viewAction("graph-delete", map2json(Map.of(
				"delete", resource
		)));
	}


	/**
	 * Leave a graph specified by a resource
	 * @param resource The resource pointing to the graph to leave.
	 * @return Success/fail response to our request. (JsonNull on sucess).
	 * @throws SpiderFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement leaveGraph(Resource resource) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.viewAction("graph-leave", map2json(Map.of("leave", resource)));
	}

	/*

  groupifyGraph(ship: Patp, name: string, toPath?: string) {
    const resource = makeResource(ship, name);
    const to = toPath && resourceFromPath(toPath);

    return this.viewAction('graph-groupify', {
      groupify: {
        resource,
        to
      }
    });
  }

*/
	public JsonElement groupifyGraph(Resource resource, @Nullable String toPath) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		Resource to = null;
		if (toPath != null) {
			to = GroupUtils.resourceFromPath(toPath);
		}
		// we have to use direct hashmap because Map.of doesn't support nulls
		Map<String, Object> groupifyObj = new HashMap<>();
		groupifyObj.put("resource", resource);
		groupifyObj.put("to", to);

		return this.viewAction("graph-groupify", map2json(Map.of(
				"groupify", groupifyObj
		)));
	}

	public JsonElement groupifyGraph(Resource resource) throws SpiderFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.groupifyGraph(resource, null);
	}

	public JsonElement eval(String cord) throws AirlockResponseError, AirlockAuthenticationError, SpiderFailureException, AirlockRequestError {
		return this.urbit.spiderRequest("graph-view-action", "tang", "graph-eval", map2json(Map.of(
				"eval", cord
		)));
	}

	/**
	 * Add a graph to a given resource.
	 * @param resource The resource to add the graph to
	 * @param graph The graph to add
	 * @param mark The mark of the graph we are adding
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	public CompletableFuture<PokeResponse> addGraph(Resource resource, Graph graph, String mark) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		return this.storeAction(map2json(Map.of(
				"add-graph", Map.of(
						"resource", resource,
						"graph", graph,
						"mark", mark
				)
		)));
	}


	/*


  addPost(ship: Patp, name: string, post: Post) {
    let nodes = {};
    nodes[post.index] = {
      post,
      children: null
    };
    return this.addNodes(ship, name, nodes);
  }
  */

	/**
	 * Add a post to a graph indicated by a resource
	 * @param resource The resource to add the post to
	 * @param post The post to add
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	public CompletableFuture<PokeResponse> addPost(Resource resource, Post post) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {

		return this.addNodes(resource, new NodeMap(Map.of(
				post.index, new Node(post, new Graph())
		)));

	}


	/**
	 * Add a single node to a graph indicated by the given resource
	 * @param resource The resource pointing to add the node to
	 * @param node The node to add
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	public CompletableFuture<PokeResponse> addNode(Resource resource, Node node) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		NodeMap nodes = new NodeMap();

		nodes.put(node.post.index, node);

		return this.addNodes(resource, nodes);

	}

	/*
  addNodes(ship: Patp, name: string, nodes: Object) {
    const action = {
      'add-nodes': {
        resource: { ship, name },
        nodes
      }
    };

    const promise = this.hookAction(ship, action);
    markPending(action['add-nodes'].nodes);
    action['add-nodes'].resource.ship = action['add-nodes'].resource.ship.slice(1);
    console.log(action);
    this.store.handleEvent({ data: { 'graph-update': action } });
    return promise;
  }
*/

	/**
	 * Add the given nodes (in the form of a {@link NodeMap}) to the specified resource
	 * @param resource The destination resource to add the nodes to
	 * @param nodes The nodes to add
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	public CompletableFuture<PokeResponse> addNodes(Resource resource, NodeMap nodes) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		final var payload = map2json(Map.of(
				"add-nodes", Map.of(
						"resource", resource,
						"nodes", nodes
				)
		));

		CompletableFuture<PokeResponse> future = this.hookAction(resource.ship, payload);

		/*
		markPending(action['add-nodes'].nodes);
		action['add-nodes'].resource.ship = action['add-nodes'].resource.ship.slice(1);
		console.log(action);

		this.store.handleEvent({ data: { 'graph-update': action } });
		*/
		markPending((List<Node>) nodes.values());
		this.updateState(payload); // we are consuming our own update in this case

		return future;
	}


	/**
	 * Remove a list of nodes specified by index
	 * @param resource The resource to remove the nodes from
	 * @param indices The indices of the nodes to remove
	 * @return A future {@link PokeResponse}
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 * @throws AirlockAuthenticationError
	 */
	public CompletableFuture<PokeResponse> removeNodes(Resource resource, List<String> indices) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		return this.hookAction(resource.ship, map2json(Map.of(
				"remove-nodes", Map.of(
						"resource", resource,
						"indices", indices
				)
		)));
	}

	/**
	 * Get all possible {@link Resource}s that our ship knows of.
	 * Keys are essentially resources, as they have the following form: {"name": graphName, "ship": owner},
	 * which is really a resource.
	 * @return The response json object which contains the keys
	 * @throws ScryDataNotFoundException
	 * @throws ScryFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonObject getKeys() throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		/*
		const keys = (json, state) => {
		  const data = _.get(json, 'keys', false);
		  if (data) {
		    state.graphKeys = new Set(data.map((res) => {
		      let resource = res.ship + '/' + res.name;
		      return resource;
		    }));
		  }
		};
		 */

		JsonObject scryResponse = this.urbit.scryRequest("graph-store", "/keys").getAsJsonObject();
		// this should be an object of the form:
		// {"graph-update": "keys": [{"name: "test", "ship": "zod"}, ...]}
		JsonObject graphUpdate = scryResponse.get("graph-update").getAsJsonObject();

		this.updateState(graphUpdate);

		return scryResponse;
	}


	/**
	 * Get all tags that our ship knows of
	 * @return Response to our request
	 * @throws ScryFailureException
	 * @throws ScryDataNotFoundException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement getTags() throws ScryFailureException, ScryDataNotFoundException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		// old_todo state handling
		// there seems to be a similar concept used with group-store tho
		// for now, the only place i see using tags is
		// https://github.com/urbit/urbit/blob/531f406222c15116c2ff4ccc6622f1eae4f2128f/pkg/interface/src/views/apps/publish/components/Writers.js

		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/tags");
		this.updateState(scryResponse.getAsJsonObject().getAsJsonObject("graph-update"));
		return scryResponse;
	}


	/**
	 * Get all tag queries.
	 * @return A success/failure of our request
	 * @throws ScryFailureException
	 * @throws ScryDataNotFoundException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement getTagQueries() throws ScryFailureException, ScryDataNotFoundException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		// Landscape doesn't do anything with tagQueries yet either

		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/tag-queries");
		this.updateState(scryResponse.getAsJsonObject().getAsJsonObject("graph-update"));
		return scryResponse;
	}

	/**
	 * Get a graph specified by a resource
	 * @param resource The resource pointing to the desired graph
	 * @return Success/fail response to our request. (JsonNull on success). NOT the graph itself.
	 * @throws ScryDataNotFoundException
	 * @throws ScryFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public JsonElement getGraph(Resource resource) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/graph/" + resource.urlForm());
		this.updateState(scryResponse.getAsJsonObject().getAsJsonObject("graph-update"));
		return scryResponse;
	}




	/**
	 * Get `n` newest nodes on a given graph.
	 * @param resource The resource to get the newest nodes from
	 * @param count The number of nodes to get
	 * @param index The index from which to start counting new nodes. Can be empty string to specify no index/latest.
	 * @throws ScryDataNotFoundException
	 * @throws ScryFailureException
	 * @throws AirlockAuthenticationError
	 * @throws AirlockResponseError
	 * @throws AirlockRequestError
	 */
	public void getNewest(Resource resource, int count, Index index) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		// todo document this better. I don't even know what it does myself
		final var data = this.urbit.scryRequest("graph-store", "/newest/" + resource.urlForm() + "/" + count + index.asString());
		this.updateState(data.getAsJsonObject().getAsJsonObject("graph-update"));
		// thing to do: look at example payload and how it is used
		// there is only one usage, which is here: https://github.com/urbit/urbit/blob/master/pkg/interface/src/views/apps/chat/ChatResource.tsx#L42
	}

	public void getNewest(Resource resource, int count) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		getNewest(resource, count, Index.createEmptyIndex());
	}

	/*

  async getOlderSiblings(ship: string, resource: string, count: number, index = '') {
    const idx = index.split('/').map(decToUd).join('/');
    const data = await this.scry<any>('graph-store',
       `/node-siblings/older/${ship}/${resource}/${count}${idx}`
     );
    this.store.handleEvent({ data });
  }
*/
	public void getOlderSiblings(Resource resource, int count, Index index) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		final var data = this.urbit.scryRequest("graph-store", "/node-siblings/older/" + resource.urlForm() + "/" + count + index.asString());
		this.updateState(data.getAsJsonObject().getAsJsonObject("graph-update"));
	}

	/*


  async getYoungerSiblings(ship: string, resource: string, count: number, index = '') {
    const idx = index.split('/').map(decToUd).join('/');
    const data = await this.scry<any>('graph-store',
       `/node-siblings/younger/${ship}/${resource}/${count}${idx}`
     );
    this.store.handleEvent({ data });
  }
	*/

	public void getYoungerSiblings(Resource resource, int count, Index index) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		final var data = this.urbit.scryRequest("graph-store", "/node-siblings/younger/" + resource.urlForm() + "/" + count + index.asString());
		this.updateState(data.getAsJsonObject().getAsJsonObject("graph-update"));
	}

	/*

  getGraphSubset(ship: string, resource: string, start: string, end: string) {
    return this.scry<any>(
      'graph-store',
      `/graph-subset/${ship}/${resource}/${end}/${start}`
    ).then((subset) => {
      this.store.handleEvent({
        data: subset
      });
    });
  }
*/
	public JsonElement getGraphSubset(Resource resource, String start, String end) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/graph-subset/" + resource.urlForm() + "/" + end + "/" + start);
		this.updateState(scryResponse.getAsJsonObject().getAsJsonObject("graph-update"));
		return scryResponse;
	}


	/*


  getNode(ship: string, resource: string, index: string) {
    const idx = index.split('/').map(numToUd).join('/');
    return this.scry<any>(
      'graph-store',
      `/node/${ship}/${resource}${idx}`
    ).then((node) => {
      this.store.handleEvent({
        data: node
      });
    });
  }
}
*/
	public JsonElement getNode(String ship, String resource, Index index) throws ScryDataNotFoundException, ScryFailureException, AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		final var data = this.urbit.scryRequest("graph-store", "/node-siblings/younger/" + ship + "/" + resource + "/" + index.asString());
		this.updateState(data.getAsJsonObject().getAsJsonObject("graph-update"));
		return data;
	}



	/**
	 * What it says on the tin. This method is responsible for updating the graph agent's state
	 * by accepting a json payload describing a `graph-update`.
	 * Most of the methods provided by {@link GraphAgent} receive a `graph-update` payload with the response data.
	 * To properly handle updating the local state, they all send that payload through this singular method
	 * (in addition to their normal behavior).
	 *
	 * Calls which do not receive a `graph-update` payload are ones which (likely) don't affect state
	 * and are usually returned directly to the caller.
	 * @param graphUpdate The payload representing the graph update
	 */

	// could also be called `reduce`
	private void updateState(@NotNull JsonObject graphUpdate) {
		// expects the object associated with the key "graph-update"
		System.out.println("Debug: Got graphUpdate obj:");
		System.out.println(graphUpdate);
		if (graphUpdate.has("keys")) {
			JsonArray keys = graphUpdate.get("keys").getAsJsonArray();
			this.keys.clear();
			/*
			 * stream(keys) => for each key:
			 * map(keyObj -> make Resource.class)
			 * then, collect to a single list
			 * then, set this.keys to that list
			 */
			this.keys.addAll(
					stream(keys.spliterator(), false)
							.map(resourceJSON -> AirlockUtils.gson.fromJson(resourceJSON, Resource.class))
							.collect(Collectors.toSet())
			);
		} else if (graphUpdate.has("add-graph")) {

			JsonObject addGraph = graphUpdate.get("add-graph").getAsJsonObject();
			Resource resource = gson.fromJson(addGraph.get("resource"), Resource.class);
			Graph newGraph = gson.fromJson(addGraph.get("graph"), Graph.class);

			Graph processedNewGraph = new Graph();


			// it seems like all it does is ensure all childrenNodes have at least a nonnull
			// `children` property that is init with an empty graph
			newGraph.forEach((index, node) -> {
				node.ensureAllChildrenNonEmpty();
				processedNewGraph.put(index, node);
			});

			this.graphs.put(resource, processedNewGraph);
			this.keys.add(resource);

		} else if (graphUpdate.has("remove-graph")) {

			JsonObject removeGraphObj = graphUpdate.get("remove-graph").getAsJsonObject();
			Resource resource = gson.fromJson(removeGraphObj, Resource.class);

			if (!this.graphs.containsKey(resource)) {
				System.out.println("Warning: Tried to remove non-existent graph");
			} else {
				this.keys.remove(resource);
				this.graphs.remove(resource);
			}

		} else if (graphUpdate.has("add-nodes")) {
			JsonObject addNodesObj = graphUpdate.getAsJsonObject("add-nodes");
			JsonObject nodesObj = addNodesObj.getAsJsonObject("nodes");
			Resource resource = gson.fromJson(addNodesObj.getAsJsonObject("resource"), Resource.class);

			System.out.println("Debug: Adding nodes");

			/*
			// I think that translating if (!this.graphs) { return; } as the following code is erroneus
			// so i have commented it out
			if (this.graphs.isEmpty()) {
				System.out.println("Warn: got add-nodes but graphs is empty");
				return;
			}
			*/
			if (!this.graphs.containsKey(resource)) {
				this.graphs.put(resource, new Graph());
			}

			this.keys.add(resource);

			nodesObj.entrySet().forEach(indexNodeEntry -> {
				// note: landscape is more flexible in regards to malformed index strings.
				// i.e., if an index doesn't parse it just stops parsing further entries
				// and returns without fanfare.
				// however, the behavior here is to just completely halt execution and throw an exception
				// when the BigInt parsing inevitably fails
				Index index = Index.fromString(indexNodeEntry.getKey());
				if (index.size() == 0) {
					return;
				}
				JsonObject nodeObj = indexNodeEntry.getValue().getAsJsonObject();
				Node node = gson.fromJson(nodeObj, Node.class);
				this.graphs.get(resource).addNode(index, node);
			});
		} else if (graphUpdate.has("remove-nodes")) {
			// indices != index.
			// index = "/1767324682374638723487987324"
			// indices = "/1767324682374638723487987324/1/4"
			// since index is a List<BigInteger>,
			// `indices` would be List<List<BigInteger>>
			// List{BitInt(1767324682374638723487987324), BigInt(1), BigInt(4)}
			JsonObject removeNodesObj = graphUpdate.getAsJsonObject("remove-nodes");
			JsonArray indicesObj = removeNodesObj.getAsJsonArray("indices");
			List<Index> indices =
					stream(indicesObj.spliterator(), false)
							.map(indexObj -> Index.fromString(indexObj.getAsString()))
							.collect(Collectors.toList());


			Resource resource = gson.fromJson(removeNodesObj.getAsJsonObject("resource"), Resource.class);
			if (!this.graphs.containsKey(resource)) {
				return;
			}

			for (Index index : indices) {
				if (index.isEmpty()) {
					System.out.println("Warning, encountered empty index: " + index);
					return;
				}
				this.graphs.get(resource).removeNode(index);
			}

		} else {
			System.out.println("Warning: encountered unknown graph-update payload. Ignoring");
		}
		// no further code should be written here because it would be skipped by early exits
	}

	public Map<Resource, Graph> getCurrentGraphs() {
		return graphs;
	}

	public Set<Resource> getCurrentKeys() {
		return this.keys;
	}

	@Override
	public String toString() {
		return "GraphAgent{" +
				"keys=" + keys +
				", graphs=" + graphs +
				'}';
	}

}
