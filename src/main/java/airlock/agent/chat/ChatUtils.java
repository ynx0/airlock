package airlock.agent.chat;

import airlock.Urbit;

import java.time.Instant;

public class ChatUtils {

	/**
	 * For reference, it creates a payload of the following form.
	 *
	 * <pre>
	 *  {@code
	 * Map<String, Object> payload = Map.of(
	 *  "message", Map.of(
	 *      "path", "/~zod/test", // different chat
	 *      "envelope", Map.of(
	 *          "uid", Urbit.uid(),
	 *          "number", 1,
	 *          "author", "~zod",
	 *          "when", Instant.now().toEpochMilli(),
	 *          "letter", Map.of("text", primaryChatViewTestMessage)
	 *      )
	 *     )
	 * );
	 * }
	 * </pre>
	 * @param path
	 * @param uid
	 * @param author
	 * @param when
	 * @param textContent
	 * @return
	 */
	public static MessagePayload createMessagePayload(String path, String uid, String author, long when, String textContent) {
		return new MessagePayload(
				new Message(
						new Envelope(
								uid,
								1, // dummy value
								author,
								when,
								new Letter(
										textContent
								)
						),
						path
				)
		);
	}

	public static MessagePayload createMessagePayload(String path, String author, long when, String textContent) {
		return ChatUtils.createMessagePayload(path, Urbit.uid(), author, when, textContent);
	}

	public static MessagePayload createMessagePayload(String path, String author, String textContent) {
		return ChatUtils.createMessagePayload(path, Urbit.uid(), author, Instant.now().toEpochMilli(), textContent);
	}

}
