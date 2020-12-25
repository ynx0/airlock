package airlock.agent.graph;

public class CodeContent extends GraphContent {
	public final String expression;
	public final String output;

	public CodeContent(String expression, String output) {
		this.expression = expression;
		this.output = output;
	}
	// todo when serialized, the payload looks like this:
	/*
		"contents": [
		            {
		              "code": {
		                "output": [
		                  [
		                    "4"
		                  ]
		                ],
		                "expression": "(add 2 2)"
		              }
		            }
	    ],

	*
	*/

}
