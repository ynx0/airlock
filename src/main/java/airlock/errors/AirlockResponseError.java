package airlock.errors;

/**
 * Thrown when there was a generic problem with the response gotten from the ship over the airlock channel
 */
public class AirlockResponseError extends AirlockChannelError {
	public AirlockResponseError(String message) {
		super(message);
	}

	public AirlockResponseError(String message, Throwable cause) {
		super(message, cause);
	}

	public AirlockResponseError(Throwable cause) {
		super(cause);
	}
}
