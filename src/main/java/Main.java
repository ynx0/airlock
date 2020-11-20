import com.google.gson.Gson;
import okhttp3.sse.EventSource;

import java.time.Instant;

public class Main {

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect();
		ship.subscribe(shipName, "chat-store", "/mailbox/~zod/mc");
		EventSource pipe = ship.getSseClient();

		String json = "{message: {path: '/~/~zod/mc', envelope: {\n" +
				"        uid: " + Urbit.uid() + ",\n" +
				"        number: 1, // Dummy, gets overwritten immediately\n" +
				"        author: '~zod',\n" +
				"        when: " + Instant.now().toEpochMilli() + ",\n" +
				"        letter: { text: 'Hello, Mars!' }\n" +
				"    }}}".trim();

		ship.poke(shipName, "chat-hook", "json", json);

		System.out.println("Done communicating with mars.");
	}

}
