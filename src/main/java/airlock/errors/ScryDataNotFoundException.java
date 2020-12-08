package airlock.errors;

public class ScryDataNotFoundException extends ScryException {

	public ScryDataNotFoundException(String message) {
		super(message);
	}

	public ScryDataNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScryDataNotFoundException(Throwable cause) {
		super(cause);
	}
}
