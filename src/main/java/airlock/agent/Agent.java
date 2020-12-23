package airlock.agent;

import airlock.PokeResponse;
import airlock.Urbit;
import airlock.errors.AirlockChannelError;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public abstract class Agent {

	protected final AgentState state;

	protected final Urbit urbit;

	// adapted from https://github.com/dclelland/UrsusAPI/
	protected Agent(Urbit urbit, AgentState state) {
		this.urbit = urbit;
		this.state = state;
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data, String ship) throws AirlockChannelError {
		return this.urbit.poke(ship, app, mark, data);
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data) throws AirlockChannelError {
		return this.urbit.poke(this.urbit.getShipName(), app, mark, data);
	}

	void unsubscribe(int subscriptionID) throws AirlockChannelError {
		this.urbit.unsubscribe(subscriptionID);
	}


}
