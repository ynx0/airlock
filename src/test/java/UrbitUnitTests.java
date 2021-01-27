import airlock.agent.graph.types.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static airlock.AirlockUtils.gson;

public class UrbitUnitTests {


	@Test
	public void validResourceFromClass() {
		Assertions.assertEquals(new Resource("~zod", "test-graph"), gson.fromJson("{\"ship\": \"~zod\",\"name\": \"test-graph\"}", Resource.class));
	}



	/*
	//		URL baseURL = new URL("http://localhost:8080/~/").toURI().normalize().toURL();
//		System.out.println(baseURL);
//		System.out.println(baseURL.toURI().resolve("/scry//" + "app/" + "/"));
//		System.out.println(baseURL.toURI().resolve("/~/channel/" + Urbit.generateChannelID()));
	 */


}
