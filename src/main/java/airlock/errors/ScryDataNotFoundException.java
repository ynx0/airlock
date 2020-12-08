package airlock.errors;

public class ScryDataNotFound extends ScryException {

	public ScryDataNotFound(String message) {
		super(message);
	}

	public ScryDataNotFound(String message, Throwable cause) {
		super(message, cause);
	}

	public ScryDataNotFound(Throwable cause) {
		super(cause);
	}
}
