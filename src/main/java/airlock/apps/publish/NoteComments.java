package airlock.apps.publish;

import airlock.agent.graph.types.Graph;
import airlock.apps.Graphable;

import java.util.Collections;
import java.util.List;

public class NoteComments implements Graphable {

	private final List<NoteComment> comments;

	public List<NoteComment> getComments() {
		// protecting the chastity^W immutability of the our list
		// idk if we should just return the mutable one but we'll find out
		return Collections.unmodifiableList(comments);
	}

	public NoteComments(List<NoteComment> comments) {
		this.comments = comments;
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
