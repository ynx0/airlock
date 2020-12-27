package airlock.agent.graph;

import org.jetbrains.annotations.Nullable;

public class Node {

	final Post post;
	final @Nullable Graph children; // technically internal graph

	public Node(Post post, @Nullable Graph children) {
		this.post = post;
		this.children = children;
	}




}
