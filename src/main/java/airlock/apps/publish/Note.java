package airlock.apps.publish;


import airlock.agent.graph.types.Graph;
import airlock.apps.Graphable;
import lombok.With;

import java.util.List;

/**
 * This class is some sort of rough analogous to apps/publish/components/Note.tsx
 * Although Note.tsx is a frontend script/file, it is a name given to the collection of the note's entry and
 * https://github.com/urbit/urbit/blob/2d50075bfb7b1d0c017d725dff9dc172d4aa8c63/pkg/interface/src/views/apps/publish/components/Note.tsx
 */
@With
// since these are not stateful objetos like `Post` or `Node` are as implemented in lanscape,
// we can have nicer immutability guarantees
public class Note implements Graphable {

	public final List<NoteRevision> revisions;
	public final NoteComments comments;

	public Note(List<NoteRevision> revisions, NoteComments comments) {
		this.revisions = revisions;
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
