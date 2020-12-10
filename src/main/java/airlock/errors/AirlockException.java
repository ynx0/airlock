package airlock.errors;

public abstract class AirlockException extends Exception {

	// https://stackoverflow.com/questions/2274102/difference-between-using-throwable-and-exception-in-a-try-catch

	public AirlockException(String message) {
		super(message);
	}

	// todo, use this type of constructor to wrap around an ioexception
	//  i.e. even if there was a failure with just executing the okhttp call,
	//  wrap that generic IOException with an AirlockException and put the throwable as the cause
	//  so that you can have a clean "throws" and "catch" statement
	public AirlockException(String message, Throwable cause) {
		super(message, cause);
	}

	public AirlockException(Throwable cause) {
		super(cause);
	}
}
