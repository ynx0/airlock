package airlock.agent.graph;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Post {
	final String author;
	final String index;
	@SerializedName("time-sent")
	final long timeSent;
	final List<GraphContent> contents;
	final @Nullable String hash;
	final List<String> signatures; // todo narrow by creating signature type

	public Post(String author, String index, long timeSent, List<GraphContent> contents, String hash, List<String> signatures) {
		this.author = author;
		this.index = index;
		this.timeSent = timeSent;
		this.contents = contents;
		this.hash = hash;
		this.signatures = signatures;
	}
}
