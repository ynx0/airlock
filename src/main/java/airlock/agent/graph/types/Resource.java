package airlock.agent.graph.types;

import airlock.types.ShipName;

import java.util.Objects;

/**
 * This class represents a reference to some object on a ship.
 * It is specified by the ship where the underlying resource is located on,
 * and the name of the resource.
 *
 * Somewhat confusingly, the hoon type for a reference to a resource is `resource`.
 * We have chosen to stick with that.
 */
public class Resource {
	public final String ship; // technically this is defined as =entity, which is a tagged union of just ship right now
	public final String name; // term

	/**
	 * Construct a resource with the specified ship and name.
	 * @param ship The ship where the underlying resource is hosted
	 * @param name The name of the resource
	 */
	public Resource(String ship, String name) {
		this.ship = ShipName.withSig(ship);
		this.name = name;
	}

	/**
	 * Turns a resource into a string in the form of "~bitbet-botbel/urbit-community"
	 * @return the url form of the resource
	 */
	public String urlForm() {
		return this.ship + "/" + this.name;
	}

	// I can't wait for record classes

	@Override
	public String toString() {
		return "Resource{" +
				"ship='" + ship + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Resource resource = (Resource) o;
		return ship.equals(resource.ship) && name.equals(resource.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ship, name);
	}
}
