import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class EyreResponseDeserializer implements JsonDeserializer<EyreResponseData> {
	@Override
	public EyreResponseData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		// todo implement
		return null;
	}
}
