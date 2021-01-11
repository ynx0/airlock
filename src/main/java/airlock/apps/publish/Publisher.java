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


	public NodeMap newPost(String title, String body, long epochCreated) {
		// adaptation of https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/lib/publish.ts#L7
		var nowDa = AirlockUtils.unixToDa(epochCreated);


		Post rootPost = new Post(
				channel.getShipName(),
				Index.fromString(nowDa.toString()),
				epochCreated,
				Collections.emptyList()
		);

		Index revisionIndex = Index.fromIndex(rootPost.index, BigInteger.ONE); // e.x: "/17007777" -> "/17007777/1"
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

	public BigInteger getLatestRevisionKey(Node node) {
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
		BigInteger latestRevisionKey = getLatestRevisionKey(node);

		if (node.children == null) {
			return null;
		}

		return node.children.get(latestRevisionKey);
	}






	// todo adapt rest of stuff like editpost here from liblogicpublish
	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/lib/publish.ts#L7
	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/apps/publish/components/EditPost.tsx#L36

}
