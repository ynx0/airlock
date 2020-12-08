package airlock.errors;

public class ShipAuthenticationError extends AirlockException {

	public ShipAuthenticationError(String message) {
		super(message);
	}

	public ShipAuthenticationError(String message, Throwable cause) {
		super(message, cause);
	}

	public ShipAuthenticationError(Throwable cause) {
		super(cause);
	}
}
