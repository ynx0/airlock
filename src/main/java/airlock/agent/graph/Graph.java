package airlock.agent.graph;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.TreeMap;

// same as BigIntOrderedMap
public class Graph extends TreeMap<BigInteger, Node> {

	// todo write proper implementation. this is not functional right now

	public static final Graph EMPTY_GRAPH = new Graph();

	public Graph() {
		super(Comparator.reverseOrder());
	}

	static class Adapter implements JsonSerializer<Graph>, JsonDeserializer<Graph> {

		@Override
		public JsonElement serialize(Graph src, Type typeOfSrc, JsonSerializationContext context) {
			if (src.equals(EMPTY_GRAPH)) {
				JsonObject empty = new JsonObject();
				// todo, see if we need {"empty": null} here
				return empty;
			} else {
				JsonArray serializedGraph = new JsonArray();
				src.forEach(((bigInteger, node) -> {
					JsonArray serializedEntry = new JsonArray();
					JsonPrimitive serializedBigInt = new JsonPrimitive(bigInteger.toString());
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
			return null;
		}
	}


}
