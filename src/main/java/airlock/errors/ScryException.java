package airlock.errors;

public class ScryException extends AirlockException {

	public ScryException(String message) {
		super(message);
	}

	public ScryException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScryException(Throwable cause) {
		super(cause);
	}
}
