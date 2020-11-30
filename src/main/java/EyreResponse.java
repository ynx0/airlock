import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * This is a data class which represents the response payload that is received from eyre after any request.
 * As a result, the structure of the properties of the class must strictly mirror the structure of the payload.
 */
public class EyreResponse {
	// todo potentially write custom deserializer that turns the "poke" -> ResponseType.POKE
//	enum ResponseType {
//		POKE,
//		SUBSCRIBE,
//		DIFF,
//		QUIT
//	}
	// adapted from https://github.com/lukechampine/go-urbit/blob/master/airlock/airlock.go#L66
	/**
	 * The id of the response. This is different from our request ids
	 */
	public int id;
	/**
	 * A string which is set to "ok" if there is no error. This shouldn't be used directly.
	 * <p>
	 * Prefer to use {@link EyreResponse#isOk()} instead
	 * </p>
	 */
	public @Nullable String ok;

	/**
	 * A string which contains an error message (usually a stack trace from hoon) if the request did not succeed
	 */
	public @Nullable String err;
	/**
	 * The type of the response which you are receiving
	 */
	public String response;

	/**
	 * The json payload associated with the response, if any
	 */
	public @Nullable JsonObject json;

	/**
	 * The success of the request
	 * @return whether or not the request was successful
	 */
	public boolean isOk() {
		return this.ok != null
				&& this.ok.equals("ok")
				&& this.err == null  // todo think about whether this would be problematic or not. i.e. should we not be this strict
				;
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
}

