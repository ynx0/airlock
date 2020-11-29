public class PokeEvent {
	public final boolean success;
	final String failureMessage;
	public static final PokeEvent SUCCESS = new PokeEvent(true, null);

	private PokeEvent(boolean success, String failureMessage) {
		this.success = success;
		this.failureMessage = failureMessage;
	}

	public static PokeEvent fromFailure(String failureMessage) {
		return new PokeEvent(false, failureMessage);
	}

	@Override
	public String toString() {
		return "PokeEvent{" +
				"success=" + success +
				", failureMessage='" + failureMessage + '\'' +
				'}';
	}
}
