package airlock.agent.invite.types;

import airlock.types.ShipName;

public class Invite {

	public final String app;
	public final String path; // type `path`
	public final String recipient; // type PatpNoSig
	public final String ship; // patpnosig
	public final String text;

	public Invite(String app, String path, String recipient, String ship, String text) {
		this.app = app;
		this.path = path;
		this.recipient = ShipName.withoutSig(recipient);
		this.ship = ShipName.withoutSig(ship);
		this.text = text;
	}
}
