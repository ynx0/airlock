package airlock.agent;

import airlock.PokeResponse;
import airlock.Urbit;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNullElse;

public abstract class Agent {

	protected final Urbit urbit;

	// adapted from https://github.com/dclelland/UrsusAPI/
	protected Agent(Urbit urbit) {
		this.urbit = urbit;
	}

	protected CompletableFuture<PokeResponse> action(String app, String mark, JsonObject data, String ship) throws IOException {
		ship = requireNonNullElse(ship, this.urbit.getShipName());
		return this.urbit.poke(ship, app, mark, data);
	}

	void unsubscribe(int subscriptionID) throws IOException {
		this.urbit.unsubscribe(subscriptionID);
	}


}
