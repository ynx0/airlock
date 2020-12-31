package airlock.agent.chat;

@Deprecated
class Message {
	public final Envelope envelope;
	public final String path;


	public Message(Envelope envelope, String path) {
		this.envelope = envelope;
		this.path = path;
	}
}
