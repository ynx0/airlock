package airlock.agent.graph;

import airlock.agent.graph.types.content.GraphContent;

public class MentionContent extends GraphContent {
	public final String mention;

	public MentionContent(String mention) {
		this.mention = mention;
	}
}
