package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.types.Resource;
import airlock.agent.graph.types.content.TextContent;
import airlock.types.ShipName;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Playground {

	public static void main(String[] args) throws Exception {


		AirlockCredentials zodCreds = new AirlockCredentials(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		AirlockCredentials sipfynCreds = new AirlockCredentials(new URL("http://localhost:80"), "sipfyn-pidmex", "toprus-dopsul-dozmep-hocbep")
		AirlockChannel urbit = new AirlockChannel(sipfynCreds);
		
		urbit.authenticate();
		urbit.connect();

		GraphAgent agent = new GraphAgent(urbit);

		long NOW = Instant.now().toEpochMilli();
		String ship = ShipName.withSig(urbit.getShipName());

//		String groupName = "test-group-" + NOW;
		//		ContactUtils.createNewGroup(urbit, groupName, new InvitePolicy(Collections.emptySet()), "Test Group", "test group description");

		Resource testGroupResource = new Resource(ship, "my-own-stuff");
		Resource testGraphResource = new Resource(ship, "test-graph-" + NOW);
		agent.createManagedGraph(
				testGraphResource.name,
				"Title of graph" + NOW,
				"description",
				testGroupResource,
				GraphAgent.Module.CHAT
		);


		CompletableFuture<PokeResponse> futurePostResponse =
				agent.addPost(
						testGraphResource,
						GraphAgent.createPost(
								ship,
								List.of(new TextContent("hey " + Instant.now()))
						)
				);

		assert futurePostResponse.get().success;

		System.out.println("Current graph of the test resource");
		System.out.println(agent.getCurrentGraphs());
		agent.getNewest(testGraphResource, 15);

		urbit.teardown();
//		System.exit(0); // need this for some reason because process never ends

		// notes dump
		/*
		failing to validate key "graph", on line /lib/graph-store/hoon:<[282 26].[282 39]>
		after reading that line, it seems to me that it is failing to turn everything inside into a node

		https://github.com/urbit/urbit/blob/5cb6af0433a65fb28b4bc957be10cb436781392d/pkg/arvo/app/graph-store.hoon#L233
		https://github.com/urbit/urbit/blob/master/pkg/interface/src/logic/api/graph.ts
		https://urbit.org/docs/tutorials/ship-troubleshooting/#reset-code lmao
		 */


		//https://github.com/dclelland/UrsusAPI/blob/master/Sources/UrsusAPI/APIs/Graph/Agents/GraphStoreAgent.swift
		// rip
		// okay so we're gonna try to create a graph instead of adding it https://github.com/urbit/urbit/blob/master/pkg/interface/src/logic/api/graph.ts#L109


		// answer was found in lib/resource.hoon. basically, you need a sig int front of the ship's name
//		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/graph/~timluc-miptev/collapse-open-blog")));
//		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/graph/" + ShipName.withSig("littel-wolfur") + "/announcements"))); // the datatype we get back from this is graphs, which is a map of resource to mark

	}

}
