import org.junit.BeforeClass;

public class UrbitTests {
	private Urbit ship;

	@BeforeClass
	public static void setup() {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		ship = new Urbit(url, shipName, code);
		ship.connect();
	}


}
