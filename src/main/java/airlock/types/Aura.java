package airlock.types;

public abstract class Aura {
	final String name;

	protected Aura(String name) {
		this.name = name;
	}

	abstract boolean validate(String representation);

	// NestsIn returns whether b nests in a.
	public boolean nestsIn(Aura other) {
		// "@uvJ".startsWith("@uv"). put in reverse, @uv is a prefix of the @uvJ
		return this.name.length() >= other.name.length() && this.name.startsWith(other.name);
	}

	/**
	 * Name with pat
	 * @return the proper name
	 */
	public String properName() {
		return "@" + this.name;
	}

}
