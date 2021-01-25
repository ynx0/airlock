package airlock.agent.group.types;

import java.util.Set;

public class InvitePolicy extends GroupPolicy {
	// invite: { pending: Set<PatpNoSig>}
	public final Set<String> pending;

	public InvitePolicy(Set<String> pending) {
		this.pending = pending;
	}

	// todo custom serializer
}
