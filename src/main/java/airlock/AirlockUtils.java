package airlock;

import airlock.agent.graph.types.Graph;
import airlock.agent.graph.types.Index;
import airlock.agent.graph.types.NodeMap;
import airlock.agent.graph.types.Post;
import airlock.agent.graph.types.content.GraphContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.HttpUrl;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class AirlockUtils {

	// todo since we use `Instant.now().toEpochMilli` a lot, maybe just extract that out to a method here

	public static final BigInteger DA_UNIX_EPOCH = new BigInteger("170141184475152167957503069145530368000"); // `@ud` ~1970.1.1
	public static final BigInteger DA_SECOND = new BigInteger("18446744073709551616"); // `@ud` ~s1
	public static final Gson gson = new GsonBuilder() // todo add some sort of lint to make sure there are no unused ADAPTER objects
			.setPrettyPrinting()  // disable in production
			.registerTypeAdapter(EyreResponse.class, EyreResponse.ADAPTER)
			.registerTypeAdapter(Graph.class, Graph.ADAPTER)
			.registerTypeAdapter(GraphContent.class, GraphContent.ADAPTER)
			.registerTypeAdapter(NodeMap.class, NodeMap.ADAPTER)
			.registerTypeAdapter(Index.class, Index.ADAPTER)
			.registerTypeAdapter(Post.class, Post.ADAPTER)
			.serializeNulls() // necessary because certain payloads that we send / receive need explicit nulls. by default gson just omits the properties which will not work
			.create();

	public static String decToUd(String ud) {
		return ud.replaceAll("/\\./g", "");
	}


	public static BigInteger unixToDa(long unix) {
		final var timeSinceEpoch = new BigInteger(String.valueOf(unix)).multiply(DA_SECOND).divide(new BigInteger(String.valueOf(1000)));
		return DA_UNIX_EPOCH.add(timeSinceEpoch);
	}

	public static JsonObject map2json(Map<String, Object> map) {
		return gson.toJsonTree(map).getAsJsonObject();
	}

	static URL escaped(String url) {
		// todo: airlock utils: use https://square.github.io/okhttp/4.x/okhttp/okhttp3/-http-url/#percent-encoding
		// which properly encodes spaces (i.e. *-> %20. currently this just throws when it encounters chars that need escaping
		return Objects.requireNonNull(HttpUrl.parse(url)).url();
	}

	static URL normalizeOrBust(URL url) {
		try {
			return url.toURI().normalize().toURL();
//			return Objects.requireNonNull(HttpUrl.parse(String.valueOf(url))).url().toURI().normalize().toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IllegalStateException("Unable to normalize url: " + url);
		}
	}

	static URL resolveOrBust(URL url, String resolve) {
		try {
//			URLEncoder.encode(resolve, StandardCharsets.UTF_8); // this encodes spaces to + like a search query rather than %20
			return url.toURI().normalize().resolve(resolve).normalize().toURL(); // do i need the second call to normalize?
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IllegalStateException("Unable to resolve url: " + url);
		}
	}
}
