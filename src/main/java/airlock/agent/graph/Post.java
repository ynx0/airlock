package airlock.agent.graph;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public class Post {
	String author;
	List<BigInteger> index;
	@SerializedName("time-sent")
	String timeSent;  // @da
	List<Content> contents;
	String hash; // @ux
	Set<Signature> signatures;
}
