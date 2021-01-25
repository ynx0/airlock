package airlock.apps.publish;

import airlock.AirlockChannel;
import airlock.AirlockUtils;
import airlock.agent.graph.types.*;
import airlock.agent.graph.types.content.TextContent;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Publisher {

	private final AirlockChannel channel;

	public Publisher(AirlockChannel channel) {
		this.channel = channel;
	}

	// this class ports the file logic/lib/publish.ts found at:
	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/lib/publish.ts#L7


	// todo take a look at mar/graph/validator/link.hoon
	// impl `indexed-post`. i think class IndexedPost extends Map.Entry<BigInteger, Post>


	public NodeMap newPost(String title, String body, long epochCreated) {
		// adaptation of https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/lib/publish.ts#L7
		var nowDa = AirlockUtils.unixToDa(epochCreated);


		Post rootPost = new Post(
				channel.getShipName(),
				new Index(nowDa),
				epochCreated,
				Collections.emptyList()
		);

		Index revisionIndex = Index.fromIndex(rootPost.index, BigInteger.ONE);
		Index commentsIndex = Index.fromIndex(rootPost.index, BigInteger.TWO);

		Post revisionsContainer = rootPost.withIndex(revisionIndex);
		Post commentsContainer = rootPost.withIndex(commentsIndex);

		var firstRevisionIndex = Index.fromIndex(revisionsContainer.index, BigInteger.ONE);
		Post firstRevision = revisionsContainer
				.withIndex(firstRevisionIndex)
				.withContents(List.of(new TextContent(title), new TextContent(body)));

		NodeMap nodes = new NodeMap(Map.of(
				rootPost.index, new Node(
						rootPost,
						new Graph(Map.of(
								BigInteger.ONE, new Node(
										revisionsContainer,
										new Graph(Map.of(
												BigInteger.ONE, new Node(
														firstRevision,
														null
												)
										))
								),
								BigInteger.TWO, new Node(
										commentsContainer,
										null
								)
						))
				)
		));

		return nodes;
	}


	public NodeMap editPost(BigInteger revision, BigInteger noteId, String title, String body, long epochCreated) {
		// implements lib/logic/publish.ts:editPost
		Post newRevision = new Post(
				channel.getShipName(),
				new Index(List.of(noteId, BigInteger.valueOf(1), revision)),
				epochCreated,
				List.of(new TextContent(title), new TextContent(body))
		);

		var nodes = new NodeMap(Map.of(
				newRevision.index, new Node(
						newRevision,
						null
				)
		));

		return nodes;
	}

	public BigInteger getLatestRevisionNum(Node node) {
		// input node: the root blogpost node
		// todo refactor keys with special schema names
		Node revisions = node.children.get(BigInteger.ONE);
		if (revisions == null) {
			return BigInteger.ONE;
		}

		if (revisions.children == null) {
			return BigInteger.ONE;
		}

		// since we have a treemap which is ordered by key, big integer first, greatest-first-least-last, the firstKey will always be the highest revision
		// this is equivalent to how landscape does it.
		// a safer approach would be to use something like ceilingKey which would guaranteed get the max entry and be the safest but
		// for now we will just blindly port landscape behavior
		return revisions.children.firstKey();
	}

	public Node getLatestRevision(Node node) {
		BigInteger latestRevisionKey = getLatestRevisionNum(node);

		if (node.children == null) {
			return null;
		}

		return node.children.get(latestRevisionKey);
	}
	// todo could combine above methods into one method which returns a nodemap which has a single entry key of id and value of Post/Node


	public BigInteger getLatestCommentRevisionNum(Node node) {
		// input node: root comment node which stores all child revisions
		if (node.children == null || node.children.isEmpty()) {
			return BigInteger.ONE;
		}
		Map.Entry<BigInteger, Node> revision = node.children.firstEntry();
		if (revision.getValue() == null) {
			return BigInteger.ONE; // again, this if guard is a blind port of landscape. i don't know if it makes sense to even test for this
		}

		return revision.getKey();
	}

	public Node getLatestCommentRevision(Node node) {
		// input node: root comment node which stores all child revisions
		BigInteger latestCommentRevisionNum = getLatestCommentRevisionNum(node);
		if (node.children == null) {
			return null;
		}
		return node.children.get(latestCommentRevisionNum);
	}


	public Node getComments(Node node) {
		// input: root blogPost node

		var comments = node.children.get(BigInteger.TWO);
		if (node.children == null) {
			return new Node(Post.buntPost(), new Graph());
		}
		return comments;
	}
	// only thing remaining as of jan 11 is `getSnippet`


	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/apps/publish/components/EditPost.tsx#L36
	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/components/Comments.tsx#L33


}
