package airlock.app.chathook;

import org.jetbrains.annotations.Nullable;

public class ChatUpdate {
	public @Nullable Message message;
	public @Nullable ChatReadMarker read;
	public @Nullable InitialUpdate initial;
}
