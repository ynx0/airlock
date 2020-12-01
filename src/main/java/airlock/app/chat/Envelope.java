package airlock.app.chat;

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
	public long when;
	/**
	 * Content of the message
	 */
	public Letter letter;

	public Envelope(String uid, int number, String author, long when, Letter letter) {
		this.uid = uid;
		this.number = number;
		this.author = author;
		this.when = when;
		this.letter = letter;
	}
}
