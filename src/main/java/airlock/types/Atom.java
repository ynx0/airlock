package airlock.types;

import java.math.BigInteger;

// adapted from https://github.com/lukechampine/go-urbit/blob/master/atom/atom.go
public class Atom {
	protected Aura aura;
	protected BigInteger value;

	public Atom(Aura aura, BigInteger value) {
		this.aura = aura;
		this.value = value;
	}

	protected void setAura(Aura newAura) {
		this.aura = newAura;
	}

	protected void setValue(BigInteger newValue) {
		this.value = newValue;
	}

	// todo better name
	public String stringRepresentation() {
		return this.value.toString();
	}

	// TODO: Basically, I'd have to implement the whole or most of
	//  https://urbit.org/docs/reference/library/4l/. Not fun :|
//	public fromString(String input, Aura targetAura) {
//		if (!targetAura.validate(input)) {
//			throw new IllegalArgumentException("error: failed to validate string: " + input + " for aura" + targetAura.properName());
//		}
//
//		return new (T) Atom();
//	}

	public static void castUnsafe(Atom inputAtom, Aura targetAura) {
		inputAtom.setAura(targetAura);
		inputAtom.setValue(inputAtom.value);
	}

	public static void cast(Atom inputAtom, Aura targetAura) throws NestFailException {
		if (!inputAtom.aura.nestsIn(targetAura)) {
			throw new NestFailException("Could not nest aura " + inputAtom.aura + " into " + targetAura);
		}
		Atom.castUnsafe(inputAtom, targetAura);
	}


}
