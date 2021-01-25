package airlock.errors.spider;

import airlock.errors.AirlockException;

public class SpiderFailureException extends AirlockException {

	public SpiderFailureException(String message) {
		super(message);
	}

	public SpiderFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpiderFailureException(Throwable cause) {
		super(cause);
	}
}
