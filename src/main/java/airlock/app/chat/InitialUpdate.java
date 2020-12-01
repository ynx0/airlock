package airlock.app.chat;

import java.util.List;

public class InitialUpdate {
	public List<Envelope> envelopes;
	public ChatConfig config;

	public InitialUpdate(List<Envelope> envelopes, ChatConfig config) {
		this.envelopes = envelopes;
		this.config = config;
	}
}
