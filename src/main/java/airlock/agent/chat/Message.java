package airlock.agent.chat;

public class Message {
	public Envelope envelope;
	public String path;


	public Message(Envelope envelope, String path) {
		this.envelope = envelope;
		this.path = path;
	}
}
