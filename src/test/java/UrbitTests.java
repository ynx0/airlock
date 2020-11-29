import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	public void helmHiSuccessful() throws IOException, InterruptedException {
//		String json = "Opening airlock :)";
////		var eventContainer = new Object() {
////			PokeEvent event = null;
////		};
//		ship.poke(ship.getShipName(), "hood", "helm-hi", json, pokeEvent -> {
//			System.out.println("Got poke event");
//			System.out.println(pokeEvent);
////			eventContainer.event = pokeEvent;
//		});
//		while (eventContainer.event == null) {
//			Thread.sleep(100);
//		}



	}


}
