package airlock.types;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ShipName {

	@NotNull
	@Contract(pure = true)
	public static String withSig(String shipName) {
		return "~" + withoutSig(shipName);
	}

	@NotNull
	private static String withoutSig(@NotNull String shipName) {
		return shipName.replace("~", "");
	}


}
