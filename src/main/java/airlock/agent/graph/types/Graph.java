package airlock.agent.graph.types;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.StreamSupport.stream;

// same as BigIntOrderedMap
public class Graph extends TreeMap<BigInteger, Node> {


	public static final Graph EMPTY_GRAPH = new Graph();

	public Graph() {
		super(Comparator.reverseOrder());
	}

	public Graph(Map<BigInteger, Node> graphMap) {
		this();
		this.putAll(graphMap);
	}

	/**
	 * N.B: this method returns a List\<BigInteger\>, not a BigInteger even though it is dealing with a single index
	 * This is because it is a more general method which is actually dealing with what I'm gonna call "DeepIndex"'s
	 * (you can also think of it as a multi-level index)
	 * Basically, they are supposed to model a nested index, such as "/17239874987324798432/4/1"
	 * which becomes List(BigInt(17239874987324798432), BigInt(4), BigInt(1))
	 * @param indexStr the index string to parse
	 * @return the resulting "deep index"
	 */
	public static List<BigInteger> indexListFromString(String indexStr) {
		return Arrays
				.stream(indexStr.split("/"))
				.skip(1)
				.map(BigInteger::new)
				.collect(Collectors.toList())
				;
	}

	public static String indexListToString(BigInteger index) {
		return indexListToString(Collections.singletonList(index));
	}

	public static String indexListToString(List<BigInteger> indexList) {
		return indexList.stream()       // 1
				.map(Objects::toString) // 2
				.collect(Collectors.joining("/", "/", "")) // 3
				;
		// 1. For each BigInteger index
		// 2. convert index to a string
		// 3. join the resulting string list into one string
		//    that is separated by slashes, has a slash at the start, and does not have anything at the end

		// example: [17012348792174, 1, 170812937724, 2] -> "/17012348792174/1/170812937724/2"


	}

	/*
	// todo potentially use wrapper method like this to clean up other code
	public BigInteger indexFromString(String indexStr) {
		return indexListFromString(indexStr).get(0);
	}
	*/

	public void addNode(List<BigInteger> deepIndex, Node node) {
		// adapted from https://github.com/urbit/urbit/blob/598a46d1f7520ed3a2fa990d223b05139a2fe344/pkg/interface/src/logic/reducers/graph-update.js#L98
		// okay so the code there is confusing, because BigIntOrderedMap.ts is mutable
		// but they treat it as if it were immutable in this equivalent function
		// (i.e. they return the graph even though it has been mutated in place)
		// for now, I won't return the new graph because it doesn't make sense
		// also, I don't get why they are calling it the parent of graph,
		// BitIntOrderedMap.get only searches the inner values, not anything outside
		// (i.e. there is no direct reference that a subgraph has to it's parent)
		// so um ???
		// also, there is a call to graph.set(index, parNode)
		// which doesn't make sense since the graph already has the node at that index
		// and Node is mutable so it is already changed ???
		BigInteger index = deepIndex.get(0);
		int indexLen = deepIndex.size();
		if (indexLen == 1) {
			this.put(index, node);
		} else {
			if (!this.containsKey(index)) {
				// todo see if we want to be this strict or just silently ignore like landscape does
				throw new IllegalStateException("parent node doesn't exist, can't add child");
			}
			// todo test thoroughly
			Node parentNode = this.get(index);
			parentNode.ensureChildGraph();
			assert parentNode.children != null;
			parentNode.children.addNode(deepIndex.subList(1, indexLen), node);
			// this.set(index, parentNode); (see above comment to why this commented out)
		}
	}
	// traverses the graph, trying to find the node specified by the deepIndex
	public void removeNode(List<BigInteger> deepIndex) {
		int indexLen = deepIndex.size();
		BigInteger currentIndex = deepIndex.get(0);

		if (indexLen == 1) {
			this.remove(currentIndex);
		} else {
			Node child = this.get(currentIndex);
			child.ensureChildGraph();

			if (child.children != null) {
				child.children.removeNode(deepIndex.subList(1, indexLen));
			} else {
				System.out.println("Warning, child graph does not exist, unable to delete node");
			}
			// todo test for thoroughness
			// im pretty sure above modifies the object in place
			// so the bottom code is not required even though it is present
			// in the javascript implementation
			// this.put(currentIndex, child);
		}
	}


	static class Adapter implements JsonSerializer<Graph>, JsonDeserializer<Graph> {

		@Override
		public JsonElement serialize(Graph src, Type typeOfSrc, JsonSerializationContext context) {
			if (src.equals(EMPTY_GRAPH)) {
				return JsonNull.INSTANCE; // results in {..., "children": null}
			} else {
				JsonArray serializedGraph = new JsonArray();
				src.forEach(((index, node) -> {
					JsonArray serializedEntry = new JsonArray();
					JsonPrimitive serializedBigInt = new JsonPrimitive("/" + index.toString());
					JsonElement serializedNode = context.serialize(node);

					serializedEntry.add(serializedBigInt);
					serializedEntry.add(serializedNode);

					serializedGraph.add(serializedEntry);
				}));
				return serializedGraph;
			}
		}

		@Override
		public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			Graph result = new Graph();
			// this code deserializes 'add-nodes', not 'add-graph'
//			JsonObject graphJson = json.getAsJsonObject();
//			graphJson.entrySet().forEach(graphEntry -> {
//				BigInteger index = Graph.indexFromString(graphEntry.getKey());
//				Node node = context.deserialize(graphEntry.getValue(), Node.class);
//				result.put(index, node);
//			});
			JsonArray graphArray = json.getAsJsonArray();
			stream(graphArray.spliterator(), false)
					.forEach(graphJSON -> {
						JsonArray graphAsArray = graphJSON.getAsJsonArray();
						String indexStr = graphAsArray.get(0).getAsString();

						BigInteger index = Graph.indexListFromString(indexStr).get(0);
						Node node = context.deserialize(graphAsArray.get(1).getAsJsonObject(), Node.class);
						result.put(index, node);
					});

			return result;
		}
	}

	public static final Adapter ADAPTER = new Adapter();

}
