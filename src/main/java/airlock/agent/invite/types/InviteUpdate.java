package airlock.agent.invite.types;

import com.google.gson.JsonObject;

public class InviteUpdate {



	// https://github.com/urbit/urbit/blob/60fc870ec0ef5a215641a635fd00192174f4c78d/pkg/interface/src/types/invite-update.ts
	public enum Type {
		INITIAL,
		CREATE,
		DELETE,
		INVITE,
		ACCEPTED,
		DECLINE
	}
	// todo maybe static inner classes instead of enum, (then instanceof check ?)

	public final Type type;
	public final JsonObject data;

	public InviteUpdate(Type type, JsonObject data) {
		this.type = type;
		this.data = data;
	}
}
