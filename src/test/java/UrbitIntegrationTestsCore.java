import airlock.AirlockCredentials;
import airlock.InMemoryResponseWrapper;
import airlock.AirlockChannel;
import airlock.errors.channel.AirlockChannelError;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrbitIntegrationTestsCore {

	private static AirlockChannel urbit;


	/* TODOs
	 * TODO add tests for subscription canceling
	 * TODO test manually canceling eventsource / deleting channel
	 */


	@BeforeAll
	public static void setup() throws MalformedURLException {
		AirlockCredentials zodCredentials = new AirlockCredentials(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		urbit = new AirlockChannel(zodCredentials);

		// Assumes fakeship zod is booted and running

	}

	@Test
	@Order(1)
	public void successfulAuthentication() throws ExecutionException, InterruptedException, AirlockChannelError {
		CompletableFuture<String> futureResponseString = new CompletableFuture<>();

		InMemoryResponseWrapper res = urbit.authenticate();
		futureResponseString.complete(res.getBodyAsString());

		await().until(futureResponseString::isDone);
		assertEquals("", futureResponseString.get());
	}

	@Test
	@Order(2)
	public void successfullyConnectToShip() {
		await().until(urbit::isAuthenticated);
		assertDoesNotThrow(() -> urbit.connect());
	}


	@Test
	@Order(3)
	public void canScry() throws Exception {
		await().until(urbit::isConnected);
		JsonElement responseJson = urbit.scryRequest("file-server", "/clay/base/hash");
		assertEquals(responseJson.getAsInt(), 0);
	}

	@Test
	@Order(4)
	public void getGraphDataFromScry() throws Exception {
		await().until(urbit::isConnected);
		JsonObject keyScry = urbit.scryRequest("graph-store", "/keys").getAsJsonObject();

		assertTrue(keyScry.has("graph-update"));
		assertTrue(keyScry.get("graph-update").getAsJsonObject().has("keys"));

	}


	@Test
	@Order(5)
	public void canSpider() throws Exception {
		await().until(urbit::isConnected);

		// todo add basic spider test.
		// it used to be graph store but graph store works and so does spider
		// but we need a basic test with a single spider request
		// in order to discern whether or not a possible regression is
		// b/c spider is failing or graph store agent is failing

	}

	@Test
	@Order(6)
	public void tearDownSuccessfully() {
		await().until(urbit::isConnected);
		// todo await until all previous tests are finished
		urbit.teardown();
		assertTrue(urbit.isAuthenticated()); // we should not lose authentication
		assertFalse(urbit.isConnected()); // we should no longer be connected
		// add in test to make sure that we can't send any further requests
	}


}
