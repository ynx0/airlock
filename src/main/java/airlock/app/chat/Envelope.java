package airlock.app.chathook;

public class Envelope {
	/**
	 * UID of the envelope
	 */
	public String uid;
	/**
	 * Message number
	 */
	public int number;
	/**
	 * Author of the message
	 */
	public String author;
	/**
	 * Unix timestamp of when the message was sent
	 */
	public int when;
	/**
	 * Content of the message
	 */
	public Letter letter;
}
