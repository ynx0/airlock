package airlock.agent.chat;

import java.util.List;

public class InitialUpdate {
	public final List<Envelope> envelopes;
	public final ChatConfig config;

	public InitialUpdate(List<Envelope> envelopes, ChatConfig config) {
		this.envelopes = envelopes;
		this.config = config;
	}
}
