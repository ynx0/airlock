import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

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
	public void shipConnects() throws Exception {
		ship.connect();
	}




}
