package airlock.agent.group.types;

import java.util.Set;

public class OpenPolicy extends GroupPolicy {
	// open: {ranks: Set<PatpNoSig>, banRanks: Set<PatpNoSig>}
	public final Set<String> ranks;
	public final Set<String> banRanks;

	public OpenPolicy(Set<String> ranks, Set<String> banRanks) {
		this.ranks = ranks;
		this.banRanks = banRanks;
	}
}
