package airlock.app.chat;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChatUpdate {
	public @Nullable Message message;
	public @Nullable ChatReadMarker read;
	/**
	 * Gotten from chat-view request
	 * key is a chat path
	 */
	public @Nullable Map<String, InitialUpdate> initial;


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
