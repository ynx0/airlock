package airlock.apps.publish;

import airlock.agent.graph.types.Graph;
import airlock.apps.Graphable;

public class NoteCommentRevision implements Graphable {

	private final String commentText;

	public NoteCommentRevision(String commentText) {
		this.commentText = commentText;
	}

	@Override
	public Graphable fromGraph(Graph source) {
		return null;
	}

	@Override
	public Graph toGraph(Graphable source) {
		return null;
	}
}
