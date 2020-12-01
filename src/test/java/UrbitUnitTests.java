import airlock.Urbit;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

public class UrbitUnitTests {

	@Test
	public void chatUtilsCreatesProperPayload() {
		// todo integration tests
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

}
