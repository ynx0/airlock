package airlock;

import java.net.URL;

public class AirlockCredentials {

	public final URL url;
	public final String ship;
	public final String code;

	/**
	 *
	 * @param url      The URL (with protocol and port) of the ship to be accessed
	 * @param ship     The name of the ship to connect to (@p)
	 * @param code     The access code for the ship at that address
	 */
	public AirlockCredentials(URL url, String ship, String code) {
		this.url = url;
		this.ship = ship;
		this.code = code;
	}

}
