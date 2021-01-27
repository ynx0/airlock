package airlock.agent.graph.types.content;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CodeContent extends GraphContent {
	public final String expression;
	public final List<String> output;

	public CodeContent(String expression, List<String> output) {
		this.expression = expression;
		this.output = output;
	}

	private static class Adapter implements JsonSerializer<CodeContent>, JsonDeserializer<CodeContent> {
		@Override
		public JsonElement serialize(CodeContent src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject result = new JsonObject();

			JsonObject expression = new JsonObject();
			JsonObject code = new JsonObject();
			JsonArray output = context.serialize(src.output).getAsJsonArray();

			code.add("output", output);
			result.add("code", code);
			result.add("expression", expression);

			return result;
		}

		@Override
		public CodeContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			Type stringList = new TypeToken<List<String>>() {
			}.getType();

			JsonObject codeObj = json.getAsJsonObject().get("code").getAsJsonObject();
			JsonArray outputObj = codeObj.getAsJsonArray("outputObj");
			JsonObject expressionObj = codeObj.getAsJsonObject("expressionObj");

			String expression = expressionObj.getAsString();
			List<String> output = context.deserialize(outputObj, stringList);
			return new CodeContent(expression, output);
		}
	}

	public static final Adapter ADAPTER = new Adapter();


	// todo when serialized, the payload looks like this:
	/*
		"contents": [
		            {
		              "code": {
		                "output": [
		                  [
		                    "4"
		                  ]
		                ],
		                "expression": "(add 2 2)"
		              }
		            }
	    ],

	*
	*/

}
