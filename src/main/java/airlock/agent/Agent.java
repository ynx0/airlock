package airlock.agent;

import airlock.Urbit;

public abstract class Agent {

	private final Urbit urbit;
	// adapted from https://github.com/dclelland/UrsusAPI/
	protected Agent(Urbit urbit) {
		this.urbit = urbit;
	}
}
