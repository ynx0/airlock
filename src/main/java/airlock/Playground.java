package airlock;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Playground {

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {

		URL baseURL = new URL("http://localhost:8080/~/").toURI().normalize().toURL();
		System.out.println(baseURL);
		System.out.println(baseURL.toURI().resolve("/scry//" + "app/" + "/"));
		System.out.println(baseURL.toURI().resolve("/~/channel/" + Urbit.generateChannelID()));

//				+ app + "/" + path + "." + mark;


	}

}
