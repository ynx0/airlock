import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public class Main {

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect();

		Main.test1(ship); // successful
		Main.test0(ship);
//		Main.test2(ship);
//		Main.test3(ship);

		System.out.println("Done communicating with mars.");
		Thread.sleep(1000);
//		ship.getSseClient().cancel();
//		System.out.println("Cancelled event source");
	}

	public static void test0(Urbit ship) {
		ship.initEventSource();
		EventSource sseClient = ship.getSseClient();
		System.out.println(sseClient.request());
	}

	public static void test1(Urbit ship) throws IOException {
		String json = "Opening airlock :)";
		ship.poke(ship.getShipName(), "hood", "helm-hi", json);
	}

	public static void test3(Urbit ship) throws IOException {


		String json = "{message: {path: '/~/~zod/mc', envelope: {\n" +
				"        uid: " + Urbit.uid() + ",\n" +
				"        number: 1,\n" +
				"        author: '~zod',\n" +
				"        when: " + Instant.now().toEpochMilli() + ",\n" +
				"        letter: { text: 'Hello, Mars!' }\n" +
				"    }}}".trim();
		ship.poke(ship.getShipName(), "chat-hook", "json", json);

	}

	public static void test2(Urbit ship) throws IOException {
		Response res = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/mc");
		ResponseBody body = res.body();
		Objects.requireNonNull(body);
		String resBodyString = body.string();
		System.out.println(resBodyString);
	}

}
