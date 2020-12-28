package airlock.agent.graph.types.content;


import com.google.gson.*;

import java.lang.reflect.Type;

// ideally this would be a sealed class or something
public abstract class GraphContent {

	static class Adapter implements JsonDeserializer<GraphContent>, JsonSerializer<GraphContent> {
		@Override
		public GraphContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			var keySet = json.getAsJsonObject().keySet();
			// todo support %mention
			if (keySet.contains("text")) {
				return context.deserialize(json, TextContent.class);
			} else if (keySet.contains("reference")) {
				return context.deserialize(json, ReferenceContent.class);
			} else if (keySet.contains("url")) {
				return context.deserialize(json, UrlContent.class);
			} else if (keySet.contains("code")) {
				return context.deserialize(json, CodeContent.class);
			} else {
				throw new JsonParseException("Invalid or unknown graph type content received: " + json);
			}
		}

		@Override
		public JsonElement serialize(GraphContent src, Type typeOfSrc, JsonSerializationContext context) {
			// todo support %mention
			if (src instanceof TextContent) {
				return context.serialize(src, TextContent.class);
			} else if (src instanceof ReferenceContent) {
				return context.serialize(src, ReferenceContent.class);
			} else if (src instanceof UrlContent) {
				return context.serialize(src, UrlContent.class);
			} else if (src instanceof CodeContent) {
				return context.serialize(src, CodeContent.class);
			} else {
				throw new JsonParseException("Tried to serialize invalid or unknown graph type: " + src);
			}
		}
	}

	public static final Adapter ADAPTER = new Adapter();


}
