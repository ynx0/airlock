public class EyreResponseData {
	// todo write custom deserializer that turns the "poke" -> ResponseType.POKE
//	enum ResponseType {
//		POKE,
//		SUBSCRIBE,
//		DIFF,
//		QUIT
//	}
	// adapted from https://github.com/lukechampine/go-urbit/blob/master/airlock/airlock.go#L66
	public int id = 0;
	public String ok;
	public String err;
	public String responseType;
	public String json;

	public boolean isOk() {
		return this.ok.equals("ok");
	}

	@Override
	public String toString() {
		return "EyreResponseData{" +
				"id=" + id +
				", ok='" + ok + '\'' +
				", err='" + err + '\'' +
				", responseType='" + responseType + '\'' +
				", json='" + json + '\'' +
				'}';
	}
}

