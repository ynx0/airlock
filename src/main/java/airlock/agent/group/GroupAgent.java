package airlock.agent.group;

import airlock.AirlockChannel;
import airlock.PokeResponse;
import airlock.agent.Agent;
import airlock.agent.graph.types.Resource;
import airlock.agent.group.types.GroupPolicy;
import airlock.errors.channel.AirlockAuthenticationError;
import airlock.errors.channel.AirlockRequestError;
import airlock.errors.channel.AirlockResponseError;
import airlock.errors.spider.SpiderFailureException;
import airlock.types.ShipName;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static airlock.AirlockUtils.map2json;

public class GroupAgent extends Agent {


	public GroupAgent(AirlockChannel channel) {
		super(channel);
		// todo subscriptions
		// pass one: blind port (done)
		// pass two: use native resource type instead of makeResource
		// pass three: figure out weird typescript types
		// pass four: reorganize, formatting, etc.
		// pass five: example unit tests
		// pass six: finalize + documentation
	}


	private CompletableFuture<PokeResponse> proxyAction(JsonObject action) throws AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.action("group-push-hook", "group-update", action);
	}

	private CompletableFuture<PokeResponse> storeAction(JsonObject action) throws AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.action("group-store", "group-update", action);
	}

	private JsonElement viewThread(String thread, JsonObject action) throws AirlockResponseError, AirlockAuthenticationError, SpiderFailureException, AirlockRequestError {
		return this.channel.spiderRequest("group-view-action", "json", thread, action);
	}

	private CompletableFuture<PokeResponse> viewAction(JsonObject action) throws AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return this.action("group-view", "group-view-action", action);
	}

	// ships is Patp (with sig)
	public CompletableFuture<PokeResponse> remove(Resource resource, List<String> ships) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.proxyAction(map2json(Map.of(
				"removeMembers", Map.of(
						"resource", resource,
						"ships", ships
				)
		)));
	}

	// todo add type tag
	// ships is Patp (with sig)
	public CompletableFuture<PokeResponse> addTag(Resource resource, String tag, List<String> ships) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.proxyAction(map2json(Map.of(
				"addTag", Map.of(
						"resource", resource,
						"tag", tag,
						"ships", ships
				)
		)));
	}

	// ships is Patp (with sig)
	public CompletableFuture<PokeResponse> removeTag(Resource resource, String tag, List<String> ships) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.proxyAction(map2json(Map.of(
				"removeTag", Map.of(
						"resource", resource,
						"tag", tag,
						"ships", ships
				)
		)));
	}

	public CompletableFuture<PokeResponse> add(Resource resource, List<String> ships) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.proxyAction(map2json(Map.of(
				"addMembers", Map.of(
						"resource", resource,
						"ships", ships
				)
		)));
	}

	public CompletableFuture<PokeResponse> removeGroup(Resource resource) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.storeAction(map2json(Map.of(
				"removeGroup", Map.of("resource", resource)
		)));
	}

	// diff Enc<GroupPolicyDiff>
	// todo: implement
//	public CompletableFuture<PokeResponse> changePolicy(Resource resource, diff) {
//		return this.proxyAction({changePolicy:{
//			resource, diff
//		} });
//	}

	public CompletableFuture<PokeResponse> join(Resource resource) throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		return this.viewAction(map2json(Map.of(
				"join", Map.of(
						"resource", resource,
						"ship", resource.ship
				)
		)));
	}

	public JsonElement create(String name, GroupPolicy policy, String title, String description) throws AirlockResponseError, AirlockAuthenticationError, SpiderFailureException, AirlockRequestError {
		return this.viewThread("group-create", map2json(Map.of(
				"create", Map.of(
						"name", name,
						"policy", policy,
						"title", title,
						"description", description
				)
		)));
	}

	public JsonElement deleteGroup(Resource resource) throws AirlockResponseError, AirlockAuthenticationError, SpiderFailureException, AirlockRequestError {
		return this.viewThread("group-delete", map2json(Map.of("remove", resource)));
	}

	public JsonElement leaveGroup(Resource resource) throws AirlockResponseError, AirlockAuthenticationError, SpiderFailureException, AirlockRequestError {
		return this.viewThread("group-leave", map2json(Map.of("leave", resource)));
	}

	public JsonElement invite(Resource resource, List<String> ships, String description) throws AirlockResponseError, AirlockAuthenticationError, SpiderFailureException, AirlockRequestError {
		// ships is patp (with sig)
		ships = ships.stream().map(ShipName::withSig).collect(Collectors.toList());
		return this.viewThread("group-invite", map2json(Map.of(
				"invite", Map.of(
						"resource", resource,
						"ships", ships,
						"description", description
				)
		)));
	}


	private void updateState(JsonObject payload) {
//		if (payload.has(""))
	}


}
