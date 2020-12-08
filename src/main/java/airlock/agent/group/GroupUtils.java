package airlock.agent.group;

import airlock.agent.graph.Resource;

public class GroupUtils {

	// implements https://github.com/urbit/urbit/blob/1895e807fdccd669dd0b514dff1c07aa3bfe7449/pkg/interface/src/logic/lib/group.ts
	// lib/group.ts
	public static Resource resourceFromPath(String pathOfGroup) {
		String[] splitPath = pathOfGroup.split("/");
		// implements
		/* const [, , ship, name] = path.split('/');
		  return { ship, name } */
		// todo check soundness

		return new Resource(
				splitPath[2],
				splitPath[3]
		);
	}


	public static Resource makeResource(String shipName, String name) {
		return new Resource(
				shipName,
				name
		);
	}
}
