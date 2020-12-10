package airlock.errors;

public class AirlockChannelError extends AirlockException{

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
