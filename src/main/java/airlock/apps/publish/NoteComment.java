package airlock.apps.publish;

import airlock.agent.graph.types.Graph;
import airlock.apps.Graphable;

import java.util.List;

// a specific comment and all of its revisions
public class NoteComment implements Graphable {

	private final List<NoteCommentRevision> revisions;

	// NoteComment higher level data schema desu
	// it then gives you a nice interface to interact with
	// but also allows you to go back to the lower level.
	// above is useless commentary btw
	public NoteComment(NoteCommentRevision commentRevision) {
		this.revisions = List.of(commentRevision);
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
