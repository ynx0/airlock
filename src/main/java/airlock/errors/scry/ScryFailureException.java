package airlock.errors.scry;

public class ScryFailureException extends ScryException {

	public ScryFailureException(String message) {
		super(message);
	}

	public ScryFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScryFailureException(Throwable cause) {
		super(cause);
	}
}
