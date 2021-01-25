package airlock.agent;

import airlock.PokeResponse;
import airlock.AirlockChannel;
import airlock.errors.channel.AirlockAuthenticationError;
import airlock.errors.channel.AirlockRequestError;
import airlock.errors.channel.AirlockResponseError;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public abstract class Agent {


	protected final AirlockChannel channel;

	// adapted from https://github.com/dclelland/UrsusAPI/
	protected Agent(AirlockChannel channel) {
		this.channel = channel;
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data, String ship) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		return this.channel.poke(ship, app, mark, data);
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		return this.channel.poke(this.channel.getShipName(), app, mark, data);
	}

	void unsubscribe(int subscriptionID) throws AirlockResponseError, AirlockRequestError, AirlockAuthenticationError {
		this.channel.unsubscribe(subscriptionID);
	}


}
