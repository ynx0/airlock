package airlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Playground {

	public static void main(String[] args) throws IOException {

//		URL baseURL = new URL("http://localhost:8080/~/").toURI().normalize().toURL();
//		System.out.println(baseURL);
//		System.out.println(baseURL.toURI().resolve("/scry//" + "app/" + "/"));
//		System.out.println(baseURL.toURI().resolve("/~/channel/" + Urbit.generateChannelID()));

		Urbit urbit = new Urbit(new URL("http://localhost:80"), "zod", "toprus-dopsul-dozmep-hocbep");
		urbit.authenticate();
//		JsonElement responseWrapper = urbit.scryRequest("graph-store", "/graph/timluc-miptev/collapse-open-blog"); // causes 404 not found, no scry result
//		JsonElement responseWrapper = urbit.scryRequest("graph-store", "/graph/timluc-miptev/collapse-open-blog");
		JsonElement responseWrapper = urbit.scryRequest("graph-store", "/graph/");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(responseWrapper));

	}

}
