public class SubscribeEvent {

	public final EventType eventType;
	public final String updateJson;
	public final String failureMessage;

	enum EventType {
		STARTED,
		FAILURE,
		UPDATE,
		FINISHED
	}

	public static final SubscribeEvent STARTED = new SubscribeEvent(EventType.STARTED, null, null);
	public static final SubscribeEvent FINISHED = new SubscribeEvent(EventType.FINISHED, null, null);

	private SubscribeEvent(EventType eventType, String updateJson, String failureMessage) {
		this.eventType = eventType;
		this.updateJson = updateJson;
		this.failureMessage = failureMessage;
	}

	public static SubscribeEvent fromUpdate(String updateJson) {
		return new SubscribeEvent(EventType.UPDATE, updateJson, null);
	}

	public static SubscribeEvent fromFailure(String failureMessage) {
		return new SubscribeEvent(EventType.FAILURE, null, failureMessage);
	}

	@Override
	public String toString() {
		return "SubscribeEvent{" +
				"eventType=" + eventType +
				", updateJson='" + updateJson + '\'' +
				", failureMessage='" + failureMessage + '\'' +
				'}';
	}
}
