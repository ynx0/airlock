package airlock.apps.publish;

import airlock.agent.graph.types.Post;
import airlock.agent.graph.types.content.GraphContent;
import airlock.agent.graph.types.content.TextContent;

import java.util.List;

// container around a title and text comprising a note
// part of the publish app
// alternative title: NoteEntry. NotePost. PublishPost
public class NoteRevision {


	String author;
	String title;
	String body;


	public NoteRevision(Post post) {
		// todo do some validation here ??

	}


	public NoteRevision(String title, String body) {

	}





	// i think toContents is wrong, toPosts makes more sense (jan 14)
//	public List<GraphContent> toContents() {
//		return List.of(new TextContent(title), new TextContent(body));
//	}


//	public Contents asContents() {
//		return List.of(new TextContent(title), new TextContent(body));
//	}
//
//	public Post asPost() {
//		return new Post();
//	}

}
