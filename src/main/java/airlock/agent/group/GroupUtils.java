package airlock.agent.group;

import airlock.agent.graph.Resource;

public class GroupUtils {

	// implements https://github.com/urbit/urbit/blob/1895e807fdccd669dd0b514dff1c07aa3bfe7449/pkg/interface/src/logic/lib/group.ts
	// lib/group.ts

	/**
	 * takes in input of the form "~landscape/ship/~bitbet-bolbel/urbit-community"
	 *
	 * @param pathOfGroup the full path to the group, including extra data from t=
	 * @return the resource that is represented by the path of the group given
	 */
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
