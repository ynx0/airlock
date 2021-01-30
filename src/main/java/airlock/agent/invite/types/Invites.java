package airlock.agent.invite.types;

import java.util.HashMap;
import java.util.Map;

//
public class Invites extends HashMap<String, AppInvites> {

	public Invites() {
		super();
	}

	public Invites(Map<? extends String, ? extends AppInvites> m) {
		super(m);
	}
}
