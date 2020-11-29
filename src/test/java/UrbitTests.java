import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrbitTests {

	private static Urbit ship;

	@BeforeAll
	public static void setup() {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		ship = new Urbit(url, shipName, code);
	}

	@Test
	@Order(1)
	public void shipConnects() {
		Assertions.assertDoesNotThrow(() -> ship.connect(), "Unable to connect to ship");

	}

	@Test
	@Order(2)
	public void helmHiSuccessful() throws IOException {
		JsonPrimitive jsonPayload = new JsonPrimitive("Opening Airlock :)");
		AtomicBoolean receivedEvent = new AtomicBoolean(false);
		ship.poke(ship.getShipName(), "hood", "helm-hi", jsonPayload, pokeEvent -> {
			System.out.println("[Poke Event]");
			System.out.println(pokeEvent);
			receivedEvent.set(true);
		});
		Assertions.assertTimeout(Duration.ofSeconds(20), () -> {
			while (!receivedEvent.get()) {
				Thread.sleep(10);
			}
		});
		assertTrue(receivedEvent.get());
	}


}
