package airlock.agent.graph.types;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the `nodes` object which you get from `payload["json"]["add-nodes"]["nodes"]`
 *
 */
public class NodeMap extends HashMap<Index, Node> {

	public NodeMap() {
		super();
	}

	public NodeMap(Map<? extends Index, ? extends Node> m) {
		super(m);
	}




	public static class Adapter implements JsonSerializer<NodeMap>, JsonDeserializer<NodeMap> {
		@Override
		public NodeMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			// hash map.
			// keys: index
			// values: Node
			NodeMap result = new NodeMap();
			JsonObject nodeMapObj = json.getAsJsonObject();
			nodeMapObj.entrySet().forEach(indexNodeObjEntry -> {
				Index indexList = Index.fromString(indexNodeObjEntry.getKey());
				Node nodeObj = context.deserialize(indexNodeObjEntry.getValue(), Node.class);
				result.put(indexList, nodeObj);
			});

			return result;
		}

		@Override
		public JsonElement serialize(NodeMap src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			src.forEach((index, node) -> {
				String indexStr = Index.toString(index);
				JsonElement nodeObj = context.serialize(node, Node.class);
				result.add(indexStr, nodeObj);
			});

			return result;
		}
	}

	public static final Adapter ADAPTER = new Adapter();
}
