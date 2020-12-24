package airlock.agent;

import airlock.PokeResponse;
import airlock.AirlockChannel;
import airlock.errors.AirlockRequestError;
import airlock.errors.AirlockResponseError;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public abstract class Agent {

	protected final AgentState state;

	protected final AirlockChannel urbit;

	// adapted from https://github.com/dclelland/UrsusAPI/
	protected Agent(AirlockChannel urbit, AgentState state) {
		this.urbit = urbit;
		this.state = state;
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data, String ship) throws AirlockResponseError, AirlockRequestError {
		return this.urbit.poke(ship, app, mark, data);
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data) throws AirlockResponseError, AirlockRequestError {
		return this.urbit.poke(this.urbit.getShipName(), app, mark, data);
	}

	void unsubscribe(int subscriptionID) throws AirlockResponseError, AirlockRequestError {
		this.urbit.unsubscribe(subscriptionID);
	}


}
