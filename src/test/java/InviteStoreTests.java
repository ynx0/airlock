import airlock.AirlockChannel;
import airlock.AirlockCredentials;
import airlock.agent.invite.InviteAgent;
import airlock.errors.channel.AirlockAuthenticationError;
import airlock.errors.channel.AirlockRequestError;
import airlock.errors.channel.AirlockResponseError;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InviteStoreTests {

	private static AirlockChannel channel;
	private static InviteAgent inviteAgent;

	@BeforeAll
	public static void setup() throws MalformedURLException {
		AirlockCredentials zodCredentials = new AirlockCredentials(new URL("http://localhost:8080"), "zod", "lidlut-tabwed-pillex-ridrup");
		channel = new AirlockChannel(zodCredentials);
		// assumes that two ships, ~zod and ~nus, are booted
		// and that zod has created a group named `test-group`, invited ~nus
	}

	@Test
	@Order(1)
	public static void canInitInviteAgent() throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
		inviteAgent = new InviteAgent(channel);
	}

	@Test
	@Order(2)
	public static void receivedInviteForTestGroup() throws AirlockRequestError, AirlockResponseError, AirlockAuthenticationError {
//		inviteAgent.getInvites();
	}



}
