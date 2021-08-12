package airlock.agent.invite;

import airlock.AirlockChannel;
import airlock.PokeResponse;
import airlock.agent.Agent;
import airlock.agent.invite.types.Invite;
import airlock.agent.invite.types.InviteUpdate;
import airlock.agent.invite.types.Invites;
import airlock.errors.channel.AirlockAuthenticationError;
import airlock.errors.channel.AirlockRequestError;
import airlock.errors.channel.AirlockResponseError;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static airlock.AirlockUtils.gson;
import static airlock.AirlockUtils.map2json;

public class InviteAgent extends Agent {

	private final Invites invites;

	public Invites getInvites() {
		return invites;
	}

	public InviteAgent(AirlockChannel channel) throws AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		super(channel);
		this.invites = new Invites();
		// you have to subscribe to "/all" to receive the initial event, so i think the following code makes sense
		// adapted from BaseSubscription and GlobalSubscription
		// this kinda sucks tho b/c the constructor has exceptions. (i feel like the exceptions are polluting everything but what can  i do?)
		// the alternative would be to have an `init` method that you'd have to call, but that wouldn't really solve the problem cause it be equivalent and make it worse because you could forget to call that method.
		this.channel.subscribe(channel.getShipName(), "invite-store", "/all", subscribeEvent -> this.updateState(subscribeEvent.updateJson));
	}

	private CompletableFuture<PokeResponse> inviteAction(JsonObject payload) throws AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.action("invite-store", "invite-action", payload);
	}

	// technically, uid is of type `serial` or @uvH, but the landscape just uses a string under the hood.
	public CompletableFuture<PokeResponse> decline(String app, String uid) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.inviteAction(map2json(Map.of(
				"decline", Map.of(
						"term", app,
						"uid", uid
				)
		)));
	}

	public CompletableFuture<PokeResponse> accept(String app, String uid) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {

		return this.inviteAction(map2json(Map.of(
				"accept", Map.of(
						"term", app,
						"uid", uid
				)
		)));
	}



	private void updateState(JsonObject payload) {
		// https://github.com/urbit/urbit/blob/60fc870ec0ef5a215641a635fd00192174f4c78d/pkg/interface/src/logic/reducers/invite-update.ts
		if (payload.has("invite-update")) {
			InviteUpdate inviteUpdate = gson.fromJson(payload.getAsJsonObject("invite-update"), InviteUpdate.class);
			switch (inviteUpdate.type) {
				case INITIAL:
					Invites invites = gson.fromJson(inviteUpdate.data.getAsJsonObject("initial"), Invites.class);
					this.invites.clear();
					this.invites.putAll(invites);
					break;
				case CREATE:
					String createPath = inviteUpdate.data
							.getAsJsonObject("create")
							.getAsJsonObject("path")
							.getAsString();
					// https://github.com/urbit/urbit/blob/60fc870ec0ef5a215641a635fd00192174f4c78d/pkg/interface/src/logic/reducers/invite-update.ts#L29
					this.invites.put(createPath, null); // todo bunt invite?? idk the appropriate port behavior
					break;
				case DELETE:
					String deletePath = inviteUpdate.data
							.getAsJsonObject("delete")
							.getAsJsonObject("path")
							.getAsString();
					this.invites.remove(deletePath);
					break;
				case INVITE:
					// wtf moment: invite-update.ts uses data.term, data.uid, data.invite while handling InviteUpdateInvite, but types/invite-update.ts states that InviteUpdateInvite is { path, uid, invite }
					// i will go with the reducer rather than the typedefs until i look at some actual payloads... :\
					JsonObject inviteData = inviteUpdate.data.getAsJsonObject("invite");
					String term = inviteData.getAsJsonObject("term").getAsString();
					String uid = inviteData.getAsJsonObject("uid").getAsString();
					Invite invite = gson.fromJson(inviteData.getAsJsonObject("invite"), Invite.class);
					this.invites.get(term).put(uid, invite);
					break;
				case ACCEPTED:
					JsonObject acceptedData = inviteUpdate.data.getAsJsonObject("accepted");
					String acceptedTerm = acceptedData.getAsJsonObject("term").getAsString();
					String acceptedUid = acceptedData.getAsJsonObject("uid").getAsString();
					this.invites.get(acceptedTerm).remove(acceptedUid);
					break;
				case DECLINE:
					JsonObject declinedData = inviteUpdate.data.getAsJsonObject("declined");
					String declinedTerm = declinedData.getAsJsonObject("term").getAsString();
					String declinedUid = declinedData.getAsJsonObject("uid").getAsString();
					this.invites.get(declinedTerm).remove(declinedUid);
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + inviteUpdate.type);
			}
		} else {
			System.err.println("[Warning]: InviteAgent got payload without invite-update");
		}
	}

}
