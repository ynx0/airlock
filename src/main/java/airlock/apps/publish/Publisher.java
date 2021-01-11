package airlock.apps.publish;

import airlock.AirlockChannel;
import airlock.AirlockUtils;
import airlock.agent.graph.types.Graph;
import airlock.agent.graph.types.Node;
import airlock.agent.graph.types.NodeMap;
import airlock.agent.graph.types.Post;
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
				Graph.indexToString(nowDa),
				epochCreated,
				Collections.emptyList()
		);

		Post revisionsContainer = rootPost.withIndex(rootPost.index + "/1");
		Post commentsContainer = rootPost.withIndex(rootPost.index + "/2");

		Post firstRevision = revisionsContainer
				.withIndex(revisionsContainer.index + "/1")
				.withContents(List.of(new TextContent(title), new TextContent(body)));

		NodeMap nodes = new NodeMap(Map.of(
				Graph.indexListFromString(rootPost.index), new Node(
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

	// todo adapt rest of stuff like editpost here from liblogicpublish
	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/logic/lib/publish.ts#L7
	// https://github.com/urbit/urbit/blob/82851feaea21cdd04d326c80c4e456e9c4f9ca8e/pkg/interface/src/views/apps/publish/components/EditPost.tsx#L36

}
