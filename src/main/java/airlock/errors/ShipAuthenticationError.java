package airlock.errors;

public class ShipAuthenticationError extends Throwable {
	public ShipAuthenticationError(String error) {
		super(error);
	}
}
