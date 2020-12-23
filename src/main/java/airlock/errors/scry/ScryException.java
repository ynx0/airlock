package airlock.errors.scry;

import airlock.errors.AirlockException;

/**
 * Thrown when there was a problem performing the scry
 */
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
