package airlock.agent.chat;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Deprecated
public class ChatUpdate {
	public @Nullable
	final
	Message message;

	public @Nullable
	final
	ChatReadMarker read;
	/**
	 * Gotten from chat-view request
	 * key is a chat path
	 */
	public @Nullable
	final
	Map<String, InitialUpdate> initial;


	public ChatUpdate(@Nullable Message message, @Nullable ChatReadMarker read, @Nullable Map<String, InitialUpdate> initial) {
		this.message = message;
		this.read = read;
		this.initial = initial;
	}

	@Override
	public String toString() {
		return "ChatUpdate{" +
				"message=" + message +
				", read=" + read +
				", initial=" + initial +
				'}';
	}
}
