package airlock.agent.graph;

public class CodeContent extends GraphContent {
	public final String expression;
	public final String output;

	public CodeContent(String expression, String output) {
		this.expression = expression;
		this.output = output;
	}
}
