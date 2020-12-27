package airlock.agent.graph;

import org.jetbrains.annotations.Nullable;

public class Node {

	Post post;
	@Nullable Graph children; // technically internal graph

	public Node(Post post, @Nullable Graph children) {
		this.post = post;
		this.children = children;
	}

	@Override
	public String toString() {
		return "Node{" +
				"post=" + post +
				", children=" + children +
				'}';
	}

	public void ensureChildGraph() {
		if (this.children == null) {
			this.children = new Graph();
		}
	}

	// todo come up with a better name lol.
	public void ensureAllChildrenHaveGraph() {
		// implements the following function found in `addGraph`
		// https://github.com/urbit/urbit/blob/598a46d1f7520ed3a2fa990d223b05139a2fe344/pkg/interface/src/logic/reducers/graph-update.js#L98
		this.ensureChildGraph();
		assert this.children != null;
		for (Node childNode : this.children.values()) {
			childNode.ensureAllChildrenHaveGraph();
		}
		/*
		const _processNode = (node) => {
		    //  is empty
		    if (!node.children) {
		      node.children = new BigIntOrderedMap();
		      return node;
		    }

		    //  is graph
		    let converted = new BigIntOrderedMap();
		    for (let idx in node.children) {
		      let item = node.children[idx];
		      let index = bigInt(idx);

		      converted.set(
		        index,
		        _processNode(item)
		      );
		    }
		    node.children = converted;
		    return node;
		  };
		 */
	}


}
