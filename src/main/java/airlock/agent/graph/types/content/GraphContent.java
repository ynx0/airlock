package airlock.agent.graph.types.content;


import airlock.agent.graph.types.Node;
import com.google.gson.*;

import java.lang.reflect.Type;

// ideally this would be a sealed class or something

/**
 * This abstract class represents the ancestor of all types of GraphContent.
 * That is, all content types which %graph-store supports within its `content` field.
 * <p>
 * <br/>
 * <br/>
 * Currently, there are 5 types of content supported:
 * <ul>
 *     <li>{@link TextContent} - Text</li>
 *     <li>{@link ReferenceContent} - Reference to another {@link Node}</li>
 *     <li>{@link UrlContent} - Any url.</li>
 *     <li>{@link CodeContent} - Content containing code that was run on a ship and its output</li>
 *     <li>{@link MentionContent} - A mention to another ship</li>
 * </ul>
 * <p>
 * <br/>
 * <br/>
 * Generally speaking, these content types are useful to client so that they may treat them differently and present them in special ways.
 * For instance, only content within a {@link UrlContent} container get treated as a url in Landscape (and are thus clickable).
 * Also, creating a TextContent with text "~zod" is not equivalent to creating MentionContent with ship "~zod".
 */
public abstract class GraphContent {

	private static class Adapter implements JsonDeserializer<GraphContent>, JsonSerializer<GraphContent> {
		@Override
		public GraphContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			var keySet = json.getAsJsonObject().keySet();
			if (keySet.contains("text")) {
				return context.deserialize(json, TextContent.class);
			} else if (keySet.contains("reference")) {
				return context.deserialize(json, ReferenceContent.class);
			} else if (keySet.contains("url")) {
				return context.deserialize(json, UrlContent.class);
			} else if (keySet.contains("code")) {
				return context.deserialize(json, CodeContent.class);
			} else if (keySet.contains("mention")) {
				return context.deserialize(json, MentionContent.class);
			} else {
				throw new JsonParseException("Invalid or unknown graph type content received: " + json);
			}
		}

		@Override
		public JsonElement serialize(GraphContent src, Type typeOfSrc, JsonSerializationContext context) {
			if (src instanceof TextContent) {
				return context.serialize(src, TextContent.class);
			} else if (src instanceof ReferenceContent) {
				return context.serialize(src, ReferenceContent.class);
			} else if (src instanceof UrlContent) {
				return context.serialize(src, UrlContent.class);
			} else if (src instanceof CodeContent) {
				return context.serialize(src, CodeContent.class);
			} else if (src instanceof MentionContent) {
				return context.serialize(src, MentionContent.class);
			} else {
				throw new JsonParseException("Tried to serialize invalid or unknown graph type: " + src);
			}
		}
	}

	public static final Adapter ADAPTER = new Adapter();


}
