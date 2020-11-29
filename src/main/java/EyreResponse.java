import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EyreResponse {
	// todo potentially write custom deserializer that turns the "poke" -> ResponseType.POKE
//	enum ResponseType {
//		POKE,
//		SUBSCRIBE,
//		DIFF,
//		QUIT
//	}
	// adapted from https://github.com/lukechampine/go-urbit/blob/master/airlock/airlock.go#L66
	public int id;
	public @Nullable String ok;
	public @Nullable String err;
	public String response;
	public @Nullable JsonObject json;

	public boolean isOk() {
		return this.ok != null && this.ok.equals("ok");
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

