package airlock.agent.graph;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

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

	public static BigInteger indexFromString(String index) {
		return new BigInteger(index.substring(1));  // strip leading slash
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
			Graph graph = new Graph();
			JsonObject graphJson = json.getAsJsonObject();
			graphJson.entrySet().forEach(graphEntry -> {
				BigInteger index = Graph.indexFromString(graphEntry.getKey());
				Node node = context.deserialize(graphEntry.getValue(), Node.class);
				graph.put(index, node);
			});
			return graph;
		}
	}

	public static final Adapter ADAPTER = new Adapter();

}
