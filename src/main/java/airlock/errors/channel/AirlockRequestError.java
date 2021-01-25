package airlock.errors.channel;

/**
 * Thrown when there was a generic problem while making the http request to the ship
 */
public class AirlockRequestError extends AirlockChannelError {
	public AirlockRequestError(String message) {
		super(message);
	}

	public AirlockRequestError(String message, Throwable cause) {
		super(message, cause);
	}

	public AirlockRequestError(Throwable cause) {
		super(cause);
	}
}
