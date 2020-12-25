package airlock;

import airlock.agent.graph.GraphAgent;
import airlock.agent.graph.Resource;
import airlock.agent.graph.TextContent;
import airlock.agent.group.GroupUtils;
import airlock.types.ShipName;

import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Playground {

	public static void main(String[] args) throws Exception {


//		String url = "http://localhost:8080";
//		String shipName = "zod";
//		String code = "lidlut-tabwed-pillex-ridrup";
		String url = "http://localhost:80";
		String shipName = "sipfyn-pidmex";
		String code = "toprus-dopsul-dozmep-hocbep";
		AirlockChannel urbit = new AirlockChannel(new URL(url), shipName, code);
		urbit.authenticate();
		urbit.connect();

		GraphAgent agent = new GraphAgent(urbit);

		long NOW = Instant.now().toEpochMilli();
		String ship = ShipName.withSig(urbit.getShipName());

//		String groupName = "test-group-" + NOW;
		String groupName = "my-own-stuff";
//		ContactUtils.createNewGroup(urbit, groupName, new InvitePolicy(Collections.emptySet()), "Test Group", "test group description");

		String graphName = "test-graph-" + NOW;
		Resource testGroup = GroupUtils.makeResource(ship, groupName);
		agent.createManagedGraph(
				graphName,
				"Title of graph" + NOW,
				"description",
				testGroup,
				GraphAgent.Module.CHAT
		);


		CompletableFuture<PokeResponse> futurePostResponse = agent.addPost(
				ship,
				graphName,
				GraphAgent.createPost(
						ship,
						Collections.singletonList(new TextContent("Hello " + NOW)),
						null,
						null
				)
		);
		assert futurePostResponse.get().success;

		urbit.teardown();


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
