package airlock.agent.graph;

public class Content {
	enum Type {
		TEXT,
		URL,
		CODE,
		REFERENCE
	}

	String text;
	String url;
	String code; // todo figure out this type. it's not correct
	String reference;

}
