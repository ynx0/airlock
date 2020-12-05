package airlock;

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

		Urbit urbit = new Urbit(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		JsonElement responseWrapper = urbit.scryRequest("graph-store", "keys");

	}

}
