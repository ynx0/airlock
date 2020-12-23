package airlock.errors;

/**
 * Thrown when there was a failure to authenticate with the ship,
 * or when we tried to request a resource which we do not have the permission to
 */
public class AirlockAuthenticationError extends AirlockChannelError {
	public AirlockAuthenticationError(String message) {
		super(message);
	}

	public AirlockAuthenticationError(String message, Throwable cause) {
		super(message, cause);
	}

	public AirlockAuthenticationError(Throwable cause) {
		super(cause);
	}
}
