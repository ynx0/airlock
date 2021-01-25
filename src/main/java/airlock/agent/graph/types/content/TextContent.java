package airlock.agent.graph.types.content;

public class TextContent extends GraphContent {
	public final String text;

	public TextContent(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "TextContent{" +
				"text='" + text + '\'' +
				'}';
	}
}
