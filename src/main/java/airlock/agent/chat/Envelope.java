package airlock.agent.chat;

@Deprecated
class Envelope {
	/**
	 * UID of the envelope
	 */
	public final String uid;
	/**
	 * Message number
	 */
	public final int number;
	/**
	 * Author of the message
	 */
	public final String author;
	/**
	 * Unix timestamp of when the message was sent
	 */
	public final long when;
	/**
	 * Content of the message
	 */
	public final Letter letter;

	public Envelope(String uid, int number, String author, long when, Letter letter) {
		this.uid = uid;
		this.number = number;
		this.author = author;
		this.when = when;
		this.letter = letter;
	}
}
