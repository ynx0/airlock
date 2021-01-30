package airlock.agent.invite.types;

import java.util.HashMap;
import java.util.Map;

// type AppInvites = { [s in Serial]: Invite; }
// type Serial = string
public class AppInvites extends HashMap<String, Invite> {
	public AppInvites() {
		super();
	}

	public AppInvites(Map<? extends String, ? extends Invite> m) {
		super(m);
	}
}
