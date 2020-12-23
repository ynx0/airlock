package airlock.errors.scry;

/**
 * Thrown when the ship was not able to find any data for the associated scry request
 */
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
