package airlock.agent.graph;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Post {
	final String author;
	final String index;
	@SerializedName("time-sent")
	final long timeSent;
	final List<Content> contents;
	final String hash;
	final List<JsonObject> signatures; // todo narrow by creating signature type

	public Post(String author, String index, long timeSent, List<Content> contents, String hash, List<JsonObject> signatures) {
		this.author = author;
		this.index = index;
		this.timeSent = timeSent;
		this.contents = contents;
		this.hash = hash;
		this.signatures = signatures;
	}
}
