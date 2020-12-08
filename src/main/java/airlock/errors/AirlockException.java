package airlock.errors;

public class AirlockException extends Exception {

	// https://stackoverflow.com/questions/2274102/difference-between-using-throwable-and-exception-in-a-try-catch

	public AirlockException(String message) {
		super(message);
	}

	public AirlockException(String message, Throwable cause) {
		super(message, cause);
	}

	public AirlockException(Throwable cause) {
		super(cause);
	}
}
