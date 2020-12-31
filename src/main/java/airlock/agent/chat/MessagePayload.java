package airlock.agent.chat;

// alternative name: MessageContainer
@Deprecated
public class MessagePayload {
	public final Message message;

	public MessagePayload(Message message) {
		this.message = message;
	}
}
