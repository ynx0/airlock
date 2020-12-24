package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.Resource;
import airlock.agent.graph.TextContent;
import airlock.agent.group.GroupUtils;
import airlock.types.ShipName;
import airlock.errors.*;
import airlock.errors.scry.*;
import airlock.errors.spider.*;

import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Playground {

	public static void main(String[] args) throws Exception {

//		URL baseURL = new URL("http://localhost:8080/~/").toURI().normalize().toURL();
//		System.out.println(baseURL);
//		System.out.println(baseURL.toURI().resolve("/scry//" + "app/" + "/"));
//		System.out.println(baseURL.toURI().resolve("/~/channel/" + Urbit.generateChannelID()));

		//  lidlut-tabwed-pillex-ridrup
		// "toprus-dopsul-dozmep-hocbep"


		String url = "http://localhost:8080";
//		String shipName = "sipfyn-pidmex";
		String shipName = "zod";
//		String code = "toprus-dopsul-dozmep-hocbep";
		String code = "lidlut-tabwed-pillex-ridrup";
		Urbit urbit = new Urbit(new URL(url), shipName, code);
		urbit.authenticate();
		urbit.connect();
		GraphAgent agent = new GraphAgent(urbit);

//		JsonArray graphKeys = agent.getKeys().getAsJsonObject()
//				.get("graph-update").getAsJsonObject()
//				.get("keys").getAsJsonArray();
//		JsonObject graphOwnedByMe = StreamSupport.stream(graphKeys.spliterator(), false)
//				.map(JsonElement::getAsJsonObject)
//				.filter(key -> key.get("ship").getAsString().equals("sipfyn-pidmex"))
//				.filter(key -> !key.get("name").getAsString().contains("dm--"))
//				.filter(key -> key.get("name").getAsString().contains("2"))
//				.findFirst().orElseThrow().getAsJsonObject();
//
//		System.out.println(graphKeys);
//		System.out.println(graphOwnedByMe);
//		JsonElement graphUpdate = agent.getGraph(ShipName.withSig(graphOwnedByMe.get("ship").getAsString()),graphOwnedByMe.get("name").getAsString());
//		System.out.println(AirlockUtils.gson.toJson(graphUpdate));
//
		long NOW = Instant.now().toEpochMilli();
		String graphName = "test-graph-" + NOW;
		String sipWithSig = ShipName.withSig(urbit.getShipName());
		Resource myOwnStuffGroup = GroupUtils.makeResource(sipWithSig, "test-group-1");
		var createGraphResponse = agent.createManagedGraph(
				graphName,
				"Title of graph" + NOW,
				"description",
				myOwnStuffGroup,
				GraphAgent.Module.CHAT
				);


		CompletableFuture<PokeResponse> futurePostResponse = agent.addPost(sipWithSig, myOwnStuffGroup.name, GraphAgent.createPost(sipWithSig, Collections.singletonList(new TextContent("Hello " + NOW)), null, null));
		futurePostResponse.get();



		// notes dump
		/*
		https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html
		https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/CharsetEncoder.html
		https://urbit.org/docs/reference/library/2i/#tap-by
		failing to validate key "graph", on line /lib/graph-store/hoon:<[282 26].[282 39]>
		after reading that line, it seems to me that it is failing to turn everything inside into a node

		https://github.com/urbit/urbit/blob/5cb6af0433a65fb28b4bc957be10cb436781392d/pkg/arvo/app/graph-store.hoon#L233
		https://github.com/urbit/urbit/blob/master/pkg/interface/src/logic/api/graph.ts
		https://urbit.org/docs/tutorials/ship-troubleshooting/#reset-code lmao
		 */

		// says unexpected poke to graph-store with mark json,
		// but when i poke with mark graph-update, it says poke-as cast fail, [%key 'ship']
		// so my go to guy apparently hasn't implemented it yet either:
		// ok wtaf I can'tfigure out the differnece between my payloads and stuff ahghghghghgh

		//https://github.com/dclelland/UrsusAPI/blob/master/Sources/UrsusAPI/APIs/Graph/Agents/GraphStoreAgent.swift
		// rip
		// okay so we're gonna try to create a graph instead of adding it https://github.com/urbit/urbit/blob/master/pkg/interface/src/logic/api/graph.ts#L109

//		long NOW = 0;
//		long NOW = Instant.now().toEpochMilli();
//		JsonObject graphPayload = AirlockUtils.gson.toJsonTree(Map.of(
//				 https://github.com/urbit/urbit/blob/531f406222c15116c2ff4ccc6622f1eae4f2128f/pkg/interface/src/views/landscape/components/NewChannel.tsx#L98
//				"create", Map.of(
//						"resource", new Resource(
//								"~zod",       // =entity
//								"test-graph" + NOW    // name=term
//						),
//						"title", "Test Graph!!!" + NOW,
//						"description", "graph for testing only! having fun strictly prohibited",
//						"associated", Map.of(
//								"group", Map.of(
//										"ship", "~zod",
//										"name", "TEST_GROUP" + NOW
//								)
//						),
//						"module", "link"
//				)
//		)).getAsJsonObject();
//		urbit.spiderRequest("graph-view-action", "graph-create", "json", graphPayload.getAsJsonObject());


		// answer was found in lib/resource.hoon. basically, you need a sig int front of the ship's name
//		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/graph/~timluc-miptev/collapse-open-blog")));
//		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/graph/" + ShipName.withSig("littel-wolfur") + "/announcements"))); // the datatype we get back from this is graphs, which is a map of resource to mark

	}

}
