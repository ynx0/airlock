/**
 * This class represents poke events that are sent by eyre in response to a poke request.
 */
public final class PokeResponse {
	/**
	 * Whether or not the poke was successful
	 */
	public final boolean success;

	/**
	 * The failure message associated with the poke, if any
	 */
	final String failureMessage;

	/**
	 * Static instance of a poke response representing a successful poke request
	 */
	public static final PokeResponse SUCCESS = new PokeResponse(true, null);

	private PokeResponse(boolean success, String failureMessage) {
		this.success = success;
		this.failureMessage = failureMessage;
	}


	public static PokeResponse fromFailure(String failureMessage) {
		return new PokeResponse(false, failureMessage);
	}

	@Override
	public String toString() {
		return "PokeEvent{" +
				"success=" + success +
				", failureMessage='" + failureMessage + '\'' +
				'}';
	}
}
