package airlock.agent.graph.types;

import java.math.BigInteger;
import java.util.List;

/**
 *  represents an object like "/1237488712342134/4/1" as List(123413241324, 4, 1)
 *  reference to an arbitrarily deep node using a List of partial Indexes, which are all BigInts
 */
public interface DeepIndex extends List<BigInteger> {
	// todo custom (de)serializer
}
