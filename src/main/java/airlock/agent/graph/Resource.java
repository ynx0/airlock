package airlock.agent.graph;

import java.util.Objects;

public class Resource {
	public final String ship; // technically this is defined as =entity, which is a tagged union of just ship right now
	public final String name; // term

	public Resource(String ship, String name) {
		this.ship = ship;
		this.name = name;
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
