package airlock.agent.chat;

@Deprecated
class ChatConfig {
	public final int length;
	public final int read;

	public ChatConfig(int length, int read) {
		this.length = length;
		this.read = read;
	}
}
