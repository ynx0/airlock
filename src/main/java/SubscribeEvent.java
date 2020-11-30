import com.google.gson.JsonObject;

/**
 * This is a data class which represents a subscribe event
 */
public class SubscribeEvent {

	/**
	 * The type of the event. See {@link SubscribeEvent.EventType} for the different types and their meanings.
	 */
	public final EventType eventType;
	public final JsonObject updateJson;
	public final String failureMessage;

	/**
	 * This enum contains possible event types
	 * <ul>
	 *  <li><b>STARTED</b> - indicates that the subscription has been started</li>
	 * </ul>
	 * <ul>
	 *  <li><b>FAILURE</b> - indicates that there was a failure while trying to subscribe. The associated event handler is removed after handling this event. </li>
	 * </ul>
	 * <ul>
	 *  <li><b>UPDATE</b> - event carries new data from the subscription</li>
	 * </ul>
	 * <ul>
	 *  <li><b>FINISHED</b> - indicates that the subscription has been terminated from the ship. The associated event handler is removed after handling this event</li>
	 * </ul>
	 */
	public enum EventType {
		STARTED,
		FAILURE,
		UPDATE,
		FINISHED
	}

	/**
	 * Static instance of the STARTED event.
	 */
	public static final SubscribeEvent STARTED = new SubscribeEvent(EventType.STARTED, null, null);
	/**
	 * Static instance of the FINISHED event.
	 */
	public static final SubscribeEvent FINISHED = new SubscribeEvent(EventType.FINISHED, null, null);

	private SubscribeEvent(EventType eventType, JsonObject updateJson, String failureMessage) {
		this.eventType = eventType;
		this.updateJson = updateJson;
		this.failureMessage = failureMessage;
	}

	public static SubscribeEvent fromUpdate(JsonObject updateJson) {
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
