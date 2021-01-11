package airlock.agent.graph.types;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class Index extends ArrayList<BigInteger>  {

	public Index() {
	}

	public Index(@NotNull Collection<? extends BigInteger> c) {
		super(c);
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

	public static String toString(Index indexList) {
		return indexList.stream()       // 1
				.map(Objects::toString) // 2
				.collect(Collectors.joining("/", "/", "")) // 3
				;
		// 1. For each BigInteger index
		// 2. convert index to a string
		// 3. join the resulting string list into one string
		//    that is separated by slashes, has a slash at the start, and does not have anything at the end

		// example: [17012348792174, 1, 170812937724, 2] -> "/17012348792174/1/170812937724/2"


	}


	public static Index fromIndex(Index baseIndex, BigInteger... restOfTheIndex) {
		List<BigInteger> intermediate = new ArrayList<>(baseIndex);
		intermediate.addAll(Arrays.asList(restOfTheIndex));

		return new Index(intermediate);
	}


}