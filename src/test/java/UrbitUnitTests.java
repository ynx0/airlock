import airlock.Urbit;
import airlock.agent.graph.Resource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

public class UrbitUnitTests {

	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Test
	public void chatUtilsCreatesProperPayload() {
		// todo unit tests
		String uid = Urbit.uid();
		String testText = "Hello World";
		Map<String, Object> targetPayload = Map.of(
				"message", Map.of(
						"path", "/~zod/test",
						"envelope", Map.of(
								"uid", uid,
								"number", 1,
								"author", "~zod",
								"when", Instant.now().toEpochMilli(),
								"letter", Map.of("text", testText)
						)
				)
		);
	}


	@Test
	public void validResourceFromClass() {
		Assertions.assertEquals(new Resource("~zod", "test-graph"), gson.fromJson("{\"ship\": \"~zod\",\"name\": \"test-graph\"}", Resource.class));
	}


}
