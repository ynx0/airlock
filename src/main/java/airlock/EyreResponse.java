package airlock;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * This is a data class which represents the response payload that is received from eyre after any request.
 * As a result, the structure of the properties of the class must strictly mirror the structure of the payload.
 */
public class EyreResponse {

	enum ResponseType {
		POKE,
		SUBSCRIBE,
		DIFF,
		QUIT
	}
	// adapted from https://github.com/lukechampine/go-urbit/blob/master/airlock/airlock.go#L66
	/**
	 * The id of the response. This is different from our request ids.
	 * The ids returned from eyre are not necessarily increasing such as ours.
	 * They are instead used as ways to refer to our transactions.
	 * So, if we make 10 requests, and have the latest request id be 10, eyre will still (by design)
	 * send back a subscription event with id 2 if that's the id of the request that wanted the subscription
	 * // todo move this documentation to somewhere more appropriate
	 */
	public final int id;

	/**
	 * The success of the request
	 */
	public final boolean ok;

	/**
	 * A string which contains an error message (usually a stack trace from hoon) if the request did not succeed
	 */
	public final @Nullable String err;

	/**
	 * The type of the response which you are receiving
	 */
	public final ResponseType response;

	/**
	 * The json payload associated with the response, if any
	 */
	public final @Nullable JsonObject json;

	private EyreResponse(int id, boolean ok, @Nullable String err, ResponseType response, @Nullable JsonObject json) {
		this.id = id;
		this.ok = ok;
		this.err = err;
		this.response = response;
		this.json = json;
	}


	@Override
	public String toString() {
		return "EyreResponseData{" +
				"id=" + id +
				", ok='" + ok + '\'' +
				", err='" + err + '\'' +
				", response='" + response + '\'' +
				", json='" + json + '\'' +
				'}';
	}

	static class Adapter implements JsonDeserializer<EyreResponse> {

		@Override
		public EyreResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject responseObject = json.getAsJsonObject();

			// todo throw proper exceptions at different failure modes

			int id = responseObject.get("id").getAsInt();
			boolean ok = responseObject.has("ok"); // right now, when response is ok, the property looks like "ok"="ok". this could change in the future
			String responseString = responseObject.get("response").getAsString();

			String err = null;
			if (responseObject.has("err")) {
				err = responseObject.get("err").getAsString();
			}

			JsonObject jsonData = null;
			if (responseObject.has("json")) {
				jsonData = responseObject.get("json").getAsJsonObject();
			}

			ResponseType responseType;
			switch (responseString) {
				case "poke":
					responseType = ResponseType.POKE;
					break;
				case "subscribe":
					responseType = ResponseType.SUBSCRIBE;
					break;
				case "diff":
					responseType = ResponseType.DIFF;
					break;
				case "quit":
					responseType = ResponseType.QUIT;
					break;
				default:
					throw new JsonParseException("Invalid Response type: " + responseString);
			}

			return new EyreResponse(id, ok, err, responseType, jsonData);
		}

		// no serializer implemented because we will never send an EyreResponse back to the ship

	}

	public static final Adapter ADAPTER = new Adapter();
}

