package airlock.agent.graph.types.content;

import airlock.types.ShipName;

public class MentionContent extends GraphContent {

	public final String mention;

	public MentionContent(String ship) {
		this.mention = ShipName.withSig(ship);
	}

}
