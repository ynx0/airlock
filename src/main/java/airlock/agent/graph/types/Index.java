package airlock.agent.graph.types;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An index is a list of one or more `uid`s (atoms) that point to a unique {@link Node} within a graph.
 * It can be seen as a unique path to the address the node with.
 * When dealing with a single node, such as adding or removing a specific node on a graph,
 * The index takes the form of "/17700000/1/2", or [17700000, 1, 2].
 * However, most {@link Index}es will be a list of length 1, because they are simply representing the current node, not a nested node.
 */
public class Index extends ArrayList<BigInteger>  {

	// todo will empty serialize properly? (i.e. an empty Index should serialize to empty string `""`) 

	/**
	 * Create an empty index
	 */
	public Index() {
		super();
	}


	/**
	 * Create an index from a list of uids
	 * @param uids The uids to create an index from
	 */
	public Index(@NotNull Collection<? extends BigInteger> uids) {
		super(uids);
	}

	/**
	 * Create an index from a single uid (BigInteger)
	 * @param singleUID The uid to create the index from
	 */
	public Index(BigInteger singleUID) {
		this(List.of(singleUID));
	}


	/**
	 * Wrapper method around empty index constructor. Exists for readability
	 * @return The freshly created empty index
	 */
	public static Index createEmptyIndex() {
		// todo come up with a better name
		return new Index();
	}

	/**
	 * N.B: this method returns a List\<BigInteger\>, not a BigInteger even though it is dealing with a single index
	 * This is because it is a more general method which is actually dealing with what I'm gonna call "DeepIndex"'s
	 * (you can also think of it as a multi-level index)
	 * Basically, they are supposed to model a nested index, such as "/17239874987324798432/4/1"
	 * which becomes List(BigInt(17239874987324798432), BigInt(4), BigInt(1))
	 * @param indexStr the index string to parse
	 * @return the resulting "deep index"
	 */
	public static Index fromString(String indexStr) {
		return (Index) Arrays
				.stream(indexStr.split("/"))
				.skip(1)
				.map(BigInteger::new)
				.collect(Collectors.toList())
				;
	}


	/**
	 * Get the canonical/serialized string representation of an index
	 * @param index The index to convert
	 * @return the canonical/serialized string representation of an index
	 */
	public static String asString(Index index) {
		if (index.size() == 0) {
			return ""; // special case: size zero index serializes to an empty string
		}

		return index.stream()       // 1
				.map(Objects::toString) // 2
				.collect(Collectors.joining("/", "/", "")) // 3
				;
		// 1. For each BigInteger index
		// 2. convert index to a string
		// 3. join the resulting string list into one string
		//    that is separated by slashes, has a slash at the start, and does not have anything at the end

		// example: [17012348792174, 1, 170812937724, 2] -> "/17012348792174/1/170812937724/2"


	}


	/**
	 * Construct an {@link Index} using an existing index as a base and a list of big integers to concatenate
	 * @param baseIndex The index to append to
	 * @param restOfTheIndex The list of uids to append to the base index
	 * @return The combination of `baseIndex` with the `restOfTheIndex`
	 */
	public static Index fromIndex(Index baseIndex, BigInteger... restOfTheIndex) {
		List<BigInteger> intermediate = new ArrayList<>(baseIndex);
		intermediate.addAll(Arrays.asList(restOfTheIndex));

		return new Index(intermediate);
	}

	/**
	 * Construct an {@link Index} using an existing index as a base and a list of big integers to concatenate
	 * @param baseIndex The index to append to
	 * @param otherIndex The index to append
	 * @return The combination of `baseIndex` with the `restOfTheIndex`
	 */
	public static Index fromIndex(Index baseIndex, Index otherIndex) {
		return fromIndex(baseIndex, otherIndex.toArray(BigInteger[]::new));
	}




	public static class Adapter implements JsonSerializer<Index>, JsonDeserializer<Index> {
		@Override
		public Index deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			String indexStr = json.getAsJsonPrimitive().getAsString();
			return Index.fromString(indexStr);
		}

		@Override
		public JsonElement serialize(Index src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(Index.asString(src));
		}
	}

	public static final Adapter ADAPTER = new Adapter();

}
