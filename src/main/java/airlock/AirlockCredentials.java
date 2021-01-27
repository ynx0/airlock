package airlock;

import airlock.types.ShipName;

import java.net.URL;

import static java.util.Objects.requireNonNull;

/**
 * The set of information needed to connect to a ship.
 */
public class AirlockCredentials {


	/**
	 * The URL representing the location where eyre is listening for requests on the ship
	 */
	public final URL url;

	/**
	 * The name of the ship that we are connecting to. (the @p without '~')
	 */
	public final String ship; // canonically, this is PatpNoSig

	/**
	 * The code is the deterministic password used to authenticate with an Urbit ship
	 * It can be obtained by running `+code` in the dojo.
	 */
	public final String code;

	/**
	 *
	 * @param url      The URL (with protocol and port) of the ship to be accessed
	 * @param ship     The name of the ship to connect to (@p)
	 * @param code     The access code for the ship at that address
	 */
	public AirlockCredentials(URL url, String ship, String code) {
		requireNonNull(url, "Please provide a url");
		requireNonNull(ship, "Please provide a ship name");
		requireNonNull(code, "Please provide a code");
		this.url = AirlockUtils.normalizeOrBust(url);
		this.ship = ShipName.withoutSig(ship); // by default, our ship name should be without a `sig`, as this is what's sent by the payload.
		this.code = code; // todo: wishlist: validate code format
	}

}
