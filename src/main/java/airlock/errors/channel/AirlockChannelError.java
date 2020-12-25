package airlock.errors.channel;

import airlock.errors.AirlockException;

/**
 *
 */
public abstract class AirlockChannelError extends AirlockException {

	public AirlockChannelError(String message) {
		super(message);
	}

	public AirlockChannelError(String message, Throwable cause) {
		super(message, cause);
	}

	public AirlockChannelError(Throwable cause) {
		super(cause);
	}
}
