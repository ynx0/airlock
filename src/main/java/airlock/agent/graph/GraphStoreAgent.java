package airlock.agent.graph;

import airlock.AirlockUtils;
import airlock.InMemoryResponseWrapper;
import airlock.PokeResponse;
import airlock.Urbit;
import airlock.agent.Agent;
import airlock.agent.group.GroupUtils;
import airlock.errors.ScryDataNotFoundException;
import airlock.errors.ScryFailureException;
import airlock.errors.ShipAuthenticationError;
import airlock.types.ShipName;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static airlock.AirlockUtils.map2json;
import static java.util.Objects.requireNonNullElse;

public class GraphStoreAgent extends Agent {

	private final Gson gson = AirlockUtils.gson;

	// adapting from new landscape api https://github.com/urbit/urbit/blob/1895e807fdccd669dd0b514dff1c07aa3bfe7449/pkg/interface/src/logic/api/graph.ts
	protected GraphStoreAgent(Urbit urbit) {
		super(urbit);
	}


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
	public static Map<String, Object> createBlankNodeWithChildPost(String shipAuthor, String parentIndex, String childIndex, List<Content> contents) {
		parentIndex = requireNonNullElse(parentIndex, "");
		childIndex = requireNonNullElse(childIndex, "");

		final var date = AirlockUtils.unixToDa(Instant.now().toEpochMilli()).toString();
		final var nodeIndex = parentIndex + '/' + date;

		final Map<String, Node> childGraph = new HashMap<>();
		// the type of this is technically InternalGraph
		childGraph.put(childIndex, new Node(
						new Post(
								ShipName.withSig(shipAuthor),
								nodeIndex + '/' + childIndex,
								Instant.now().toEpochMilli(),
								contents,
								null,
								Collections.emptyList()
						),
						Map.of(
								"empty", Optional.empty()  // {empty: null}
						)
				)
		);
		return Map.of(
				"post", new Post(
						ShipName.withSig(shipAuthor),
						nodeIndex,
						Instant.now().toEpochMilli(),
						Collections.emptyList(),
						null,
						Collections.emptyList()
				),
				"children", childGraph
		);
	}


	/*
	* function markPending(nodes: any) {
  _.forEach(nodes, node => {
    node.post.author = deSig(node.post.author);
    node.post.pending = true;
    markPending(node.children || {});
  });
}
*/

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

	public static Object createPost(String shipAuthor, List<Content> contents, String parentIndex, String childIndex) {
		parentIndex = requireNonNullElse(parentIndex, "");
		childIndex = requireNonNullElse(childIndex, "DATE_PLACEHOLDER");
		if (childIndex.equals("DATE_PLACEHOLDER")) {
			childIndex = AirlockUtils.unixToDa(Instant.now().toEpochMilli()).toString();
		}

		return new Post(
				shipAuthor,
				parentIndex + "/" + childIndex,
				Instant.now().toEpochMilli(),
				contents,
				null,
				Collections.emptyList()
		);

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
	public enum Modules {
		LINK("graph-validator-link"),
		PUBLISH("graph-validator-publish"),
		CHAT("graph-validator-chat");

		public final String mark;

		Modules(String mark) {
			this.mark = mark;
		}

		public String moduleName() {
			return this.name().toLowerCase();
		}

	}

	static String moduleToMark(Modules module) {
		return module.mark;
	}

	/*

  private storeAction(action: any): Promise<any> {
    return this.action('graph-store', 'graph-update', action)
  }
  */
	private CompletableFuture<PokeResponse> storeAction(JsonObject payload) throws IOException {
		return this.action("graph-store", "graph-update", payload, null);
	}

	/*
	  private viewAction(threadName: string, action: any) {
		return this.spider('graph-view-action', 'json', threadName, action);
	  }
	*/
	private InMemoryResponseWrapper viewAction(String threadName, JsonObject payload) throws IOException {
		return this.urbit.spiderRequest("graph-view-action", threadName, "json", payload);
	}

	/*
  private hookAction(ship: Patp, action: any): Promise<any> {
    return this.action('graph-push-hook', 'graph-update', action);
  }

*/
	private CompletableFuture<PokeResponse> hookAction(String ship, JsonObject payload) throws IOException {
		return this.action("graph-push-hook", "graph-update", payload, null);
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
	public InMemoryResponseWrapper createManagedGraph(String name, String title, String description, String pathOfGroup, Modules module) throws IOException {
		final var associated = Map.of("group", GroupUtils.resourceFromPath(pathOfGroup));
		final var resource = GroupUtils.makeResource(this.urbit.getShipName(), name);

		return this.viewAction("graph-create", gson.toJsonTree(Map.of(
				"create", Map.of(
						"resource", resource,
						"title", title,
						"description", description,
						"associated", associated,
						"module", module.moduleName(),
						"mark", moduleToMark(module)
				)
		)).getAsJsonObject());
	}

	/*
	// todo implement createUnmanagedGraph
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


	/*
  joinGraph(ship: Patp, name: string) {
    const resource = makeResource(ship, name);
    return this.viewAction('graph-join', {
      join: {
        resource,
        ship,
      }
    });
  }
*/
	public InMemoryResponseWrapper joinGraph(String ship, String name) throws IOException {
		final var resource = GroupUtils.makeResource(ship, name);
		return this.viewAction("graph-join", map2json(Map.of(
				"join", Map.of(
						"resource", resource,
						"ship", ship
				)
		)));
	}

	/*
  deleteGraph(name: string) {
    const resource = makeResource(`~${window.ship}`, name);
    return this.viewAction('graph-delete', {
      "delete": {
        resource
      }
    });
  }
*/
	public InMemoryResponseWrapper deleteGraph(String name) throws IOException {
		final var resource = GroupUtils.makeResource(this.urbit.getShipName(), name);
		return this.viewAction("graph-delete", map2json(Map.of(
				"delete", resource
		)));
	}


	/*
  leaveGraph(ship: Patp, name: string) {
    const resource = makeResource(ship, name);
    return this.viewAction('graph-leave', {
      "leave": {
        resource
      }
    });
  }

*/
	public InMemoryResponseWrapper leaveGraph(String ship, String name) throws IOException {
		final var resource = GroupUtils.makeResource(this.urbit.getShipName(), name);
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
	public InMemoryResponseWrapper groupifyGraph(String ship, String name, Optional<String> toPath) throws IOException {
		final var resource = GroupUtils.makeResource(this.urbit.getShipName(), name);

		//GroupUtils::resourceFromPath
		final Optional<Resource> to = toPath.map(GroupUtils::resourceFromPath);
		return this.viewAction("graph-groupify", map2json(Map.of(
				"groupify", Map.of(
						"resource", resource,
						"to", to
				)
		)));
	}

	/*

  addGraph(ship: Patp, name: string, graph: any, mark: any) {
    return this.storeAction({
      'add-graph': {
        resource: { ship, name },
        graph,
        mark
      }
    });
  }
  */
	public CompletableFuture<PokeResponse> addGraph(String ship, String name, Map<String, Object> graph, String mark) throws IOException {
		return this.storeAction(map2json(Map.of(
				"add-graph", Map.of(
						"resource", Map.of("ship", ship, "name", name),
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
	public CompletableFuture<PokeResponse> addPost(String ship, String name, Post post) throws IOException {
		Map<String, Object> nodes = new HashMap<>();
//		new Node(post, null);
		nodes.put(post.index, map2json(Map.of(
				"post", post,
				"children", Optional.empty()
		)));

		return this.addNodes(ship, name, nodes);

	}


	/*



  addNode(ship: Patp, name: string, node: Object) {
    let nodes = {};
    nodes[node.post.index] = node;

    return this.addNodes(ship, name, nodes);
  }
*/
	public CompletableFuture<PokeResponse> addNode(String ship, String name, Node node) throws IOException {
		Map<String, Object> nodes = new HashMap<>();

		nodes.put(node.post.index, node);

		return this.addNodes(ship, name, nodes);

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
	public CompletableFuture<PokeResponse> addNodes(String ship, String name, Map<String, Object> nodes) throws IOException {
		final var payload = map2json(Map.of(
				"add-nodes", Map.of(
						"resource", Map.of("ship", ship, "name", name),
						"nodes", nodes
				)
		));

		CompletableFuture<PokeResponse> future = this.hookAction(ship, payload);

		// todo implement other client-side effects to handle state
		/*
		markPending(action['add-nodes'].nodes);
		action['add-nodes'].resource.ship = action['add-nodes'].resource.ship.slice(1);
		console.log(action);
		this.store.handleEvent({ data: { 'graph-update': action } });
		*/

		return future;
	}

	/*


  removeNodes(ship: Patp, name: string, indices: string[]) {
    return this.hookAction(ship, {
      'remove-nodes': {
        resource: { ship, name },
        indices
      }
    });
  }
*/
	public CompletableFuture<PokeResponse> removeNodes(String ship, String name, String[] indices) throws IOException {
		return this.hookAction(ship, map2json(Map.of(
				"remove-nodes", Map.of(
						"resource", Map.of("ship", ship, "name", name),
						"indices", indices
				)
		)));
	}

	/*


  getKeys() {
    return this.scry<any>('graph-store', '/keys')
      .then((keys) => {
        this.store.handleEvent({
          data: keys
        });
      });
  }

*/
	public JsonElement getKeys() throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/keys");
		// todo implement state handling
		/*
		this.store.handleEvent({
          data: keys
        });
		 */
		return scryResponse;
	}


	/*

  getTags() {
    return this.scry<any>('graph-store', '/tags')
      .then((tags) => {
        this.store.handleEvent({
          data: tags
        });
      });
  }

*/

	public JsonElement getTags() throws IOException, ScryFailureException, ShipAuthenticationError, ScryDataNotFoundException {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/tags");
		// todo state handling
		return scryResponse;
	}

	/*

  getTagQueries() {
    return this.scry<any>('graph-store', '/tag-queries')
      .then((tagQueries) => {
        this.store.handleEvent({
          data: tagQueries
        });
      });
  }

*/
	public JsonElement getTagQueries() throws IOException, ScryFailureException, ShipAuthenticationError, ScryDataNotFoundException {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/tag-queries");
		// todo implement state handling
		return scryResponse;
	}

	/*

  getGraph(ship: string, resource: string) {
    return this.scry<any>('graph-store', `/graph/${ship}/${resource}`)
      .then((graph) => {
        this.store.handleEvent({
          data: graph
        });
      });
  }
*/
	public JsonElement getGraph(String ship, String resource) throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/graph/" + ship + "/" + resource);
		// todo implement state handling
		return scryResponse;
	}

	/*
  async getNewest(ship: string, resource: string, count: number, index = '') {
    const data = await this.scry<any>('graph-store', `/newest/${ship}/${resource}/${count}${index}`);
    this.store.handleEvent({ data });
  }
	*/

	public void getNewest(String ship, String resource, int count, String index) throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		final var data = this.urbit.scryRequest("graph-store", "/newest/" + ship + "/" + resource + "/" + count + index);
		// todo state handling
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
	public void getOlderSiblings(String ship, String resource, int count, String index) throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		final var idx = Arrays.stream(index.split("/")).map(AirlockUtils::decToUd).collect(Collectors.joining("/"));
		final var data = this.urbit.scryRequest("graph-store", "/node-siblings/older/" + ship + "/" + resource + "/" + count + idx);
		// todo handle effect
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

	public void getYoungerSiblings(String ship, String resource, int count, String index) throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		final var idx = Arrays.stream(index.split("/")).map(AirlockUtils::decToUd).collect(Collectors.joining("/"));
		final var data = this.urbit.scryRequest("graph-store", "/node-siblings/younger/" + ship + "/" + resource + "/" + count + idx);
		// todo handle storage

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
	public JsonElement getGraphSubset(String ship, String resource, String start, String end) throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		JsonElement scryResponse = this.urbit.scryRequest("graph-store", "/graph-subset/" + ship + "/" + resource + "/" + end + "/" + "start");
		// todo handle storage
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
	public JsonElement getNode(String ship, String resource, String index) throws IOException, ScryDataNotFoundException, ShipAuthenticationError, ScryFailureException {
		final var idx = Arrays.stream(index.split("/")).map(AirlockUtils::decToUd).collect(Collectors.joining("/"));
		final var data = this.urbit.scryRequest("graph-store", "/node-siblings/younger/" + ship + "/" + resource + "/" + idx);
		// todo handle storage
		return data;
	}


}
