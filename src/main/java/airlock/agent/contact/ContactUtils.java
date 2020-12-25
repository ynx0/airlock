package airlock.agent.contact;

import airlock.AirlockChannel;
import airlock.AirlockUtils;
import airlock.PokeResponse;
import airlock.agent.group.types.GroupPolicy;
import airlock.errors.AirlockAuthenticationError;
import airlock.errors.AirlockRequestError;
import airlock.errors.AirlockResponseError;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ContactUtils {

	// one off utility class, not meant to be permanent

	public static CompletableFuture<PokeResponse> createNewGroup(AirlockChannel channel, String name, GroupPolicy groupPolicy, String title, String description) throws AirlockAuthenticationError, AirlockResponseError, AirlockRequestError {
		return channel.poke(channel.getShipName(), "contact-view", "json", AirlockUtils.map2json(Map.of(
				"create", Map.of(
						"name", name,
						"policy", groupPolicy
				),
				"title", title,
				"description", description
		)));
	}

}
