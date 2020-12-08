package airlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.util.Map;

public class AirlockUtils {

	public static final BigInteger DA_UNIX_EPOCH = new BigInteger("170141184475152167957503069145530368000"); // `@ud` ~1970.1.1
	public static final BigInteger DA_SECOND = new BigInteger("18446744073709551616"); // `@ud` ~s1
	public static final Gson gson = new GsonBuilder()
			.setPrettyPrinting()  // disable in production
			.registerTypeAdapter(EyreResponse.class, EyreResponse.ADAPTER)
			.serializeNulls() // necessary for certain payloads that we send
			.create();

	public static String decToUd(String ud) {
		return ud.replaceAll("/\\./g", "");
	}


	public static BigInteger unixToDa(long unix) {
		final var timeSinceEpoch = new BigInteger(String.valueOf(unix)).multiply(DA_SECOND).divide(new BigInteger(String.valueOf(1000)));
		return DA_UNIX_EPOCH.add(timeSinceEpoch);
	}

	@SuppressWarnings("rawtypes")
	public static JsonObject map2json(Map map) {
		return gson.toJsonTree(map).getAsJsonObject();
	}


}
