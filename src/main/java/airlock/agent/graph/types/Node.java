package airlock.agent.graph.types;

import airlock.types.ShipName;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * This class represents a particular node of a graph.
 */
@With
public class Node {

	public @NotNull
	Post post;
	public @Nullable
	Graph children; // technically internal graph


	public Node(@NotNull Post post, @Nullable Graph children) {
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

	/**
	 * Recursively ensure that no node in any of the children has a {@code null} child.
	 */
	public void ensureAllChildrenNonEmpty() {
		// implements the following function found in `addGraph`
		// https://github.com/urbit/urbit/blob/598a46d1f7520ed3a2fa990d223b05139a2fe344/pkg/interface/src/logic/reducers/graph-update.js#L98
		this.ensureChildGraph();
		assert this.children != null;
		// if the children graph is empty then we will stop the recursion
		for (Node childNode : this.children.values()) {
			childNode.ensureAllChildrenNonEmpty();
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

	/**
	 * Mark the current post as pending. Directly ported from Landscape
	 */
	public void markPending() {
		this.post.author = ShipName.withoutSig(this.post.author); // todo see if this is even necessary
		this.post.setPending(true);
		if (this.children != null) {
			for (Node node : this.children.values()) {
				node.markPending();
			}
		}
	}


}
