package airlock.agent.graph;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.TreeMap;

// same as BigIntOrderedMap
public class Graph extends TreeMap<BigInteger, Node> {


	public static final Graph EMPTY_GRAPH = new Graph();

	public Graph() {
		super(Comparator.reverseOrder());
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
				String indexString = graphEntry.getKey().substring(1); // strip leading slash
				BigInteger index = new BigInteger(indexString);
				Node node = context.deserialize(graphEntry.getValue(), Node.class);
				graph.put(index, node);
			});
			return graph;
		}
	}

	public static final Adapter ADAPTER = new Adapter();

}
