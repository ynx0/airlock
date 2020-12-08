package airlock.agent.graph;

import java.util.Map;

public class Node {

	final Post post;
	final Map<String, Object> children;

	public Node(Post post, Map<String, Object> children) {
		this.post = post;
		this.children = children;
	}
}
