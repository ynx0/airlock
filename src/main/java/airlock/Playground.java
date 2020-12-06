package airlock;

import airlock.types.ShipName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class Playground {

	public static void main(String[] args) throws IOException {

//		URL baseURL = new URL("http://localhost:8080/~/").toURI().normalize().toURL();
//		System.out.println(baseURL);
//		System.out.println(baseURL.toURI().resolve("/scry//" + "app/" + "/"));
//		System.out.println(baseURL.toURI().resolve("/~/channel/" + Urbit.generateChannelID()));

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//  lidlut-tabwed-pillex-ridrup
		// "toprus-dopsul-dozmep-hocbep"
		Urbit urbit = new Urbit(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		urbit.authenticate();
		urbit.connect();
		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/keys")));

		// notes dump
		/*
		https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html
		https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/CharsetEncoder.html
		https://urbit.org/docs/reference/library/2i/#tap-by
		failing to validate key "graph", on line /lib/graph-store/hoon:<[282 26].[282 39]>
		https://github.com/urbit/urbit/blob/5cb6af0433a65fb28b4bc957be10cb436781392d/pkg/arvo/app/graph-store.hoon#L233
		https://github.com/urbit/urbit/blob/master/pkg/interface/src/logic/api/graph.ts
		https://urbit.org/docs/tutorials/ship-troubleshooting/#reset-code lmao
		 */

		// says unexpected poke to graph-store with mark json,
		// but when i poke with mark graph-update, it says poke-as cast fail, [%key 'ship']
	// so my go to guy apparently hasn't implemented it yet either:
		//https://github.com/dclelland/UrsusAPI/blob/master/Sources/UrsusAPI/APIs/Graph/Agents/GraphStoreAgent.swift
		// rip
//		urbit.poke(urbit.getShipName(), "graph-store", "graph-update", gson.toJsonTree(Map.of(
//				"add-graph", Map.of(
//						"overwrite", true,
//						"resource", Map.of(
//								"ship", "~zod",       // =entity
//								"name", "test-graph"    // name=term
//						),
//						"mark", "graph-validator-publish",
//						"graph", Arrays.asList(
//								"/1",
//								Map.of(
//										"post", Map.of(
//												"index", "/1",
//												"author", "zod",
//												"time-sent", Instant.now().toEpochMilli(),
//												"signatures", Collections.emptyList(),
//												"contents", Collections.singletonList(Map.of("text", "Hello Graph!"))
//										),
//										"children", Collections.emptyList()
//								)
//
//						)
//				)
//		)));

		// answer was found in lib/resource.hoon. basically, you need a sig int front of the ship's name
//		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/graph/~timluc-miptev/collapse-open-blog")));
//		System.out.println(gson.toJson(urbit.scryRequest("graph-store", "/graph/" + ShipName.withSig("littel-wolfur") + "/announcements"))); // the datatype we get back from this is graphs, which is a map of resource to mark

	}

}
