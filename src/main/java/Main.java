import java.io.IOException;

public class Main {

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect();

		Main.test1(ship);

		System.out.println("Done communicating with mars.");
	}

	public static void test1(Urbit ship) throws IOException {
		String json = "Opening airlock :)";
		ship.poke(ship.getShipName(), "hood", "helm-hi", json);
	}

	public static void test2(Urbit ship) throws IOException {
//		ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/mc");
//		EventSource pipe = ship.getSseClient();
//
//		String json = "{message: {path: '/~/~zod/mc', envelope: {\n" +
//				"        uid: " + Urbit.uid() + ",\n" +
//				"        number: 1,\n" +
//				"        author: '~zod',\n" +
//				"        when: " + Instant.now().toEpochMilli() + ",\n" +
//				"        letter: { text: 'Hello, Mars!' }\n" +
//				"    }}}".trim();
//		ship.poke(shipName, "chat-hook", "json", json);
	}

}
