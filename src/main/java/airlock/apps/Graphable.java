package airlock.apps;

import airlock.agent.graph.types.Graph;

/**
 * This interface represents any type that is able to go to/from a higher level representation to a `Graph`
 * Right now, the apps chat, collections, and notebook all have objects (chat message, link entry, note)
 * that can be represented as (and thus marshalled in and out of) `Graphs`
 */
public interface Graphable {

	Graphable fromGraph(Graph source);
	Graph toGraph(Graphable source);

}
