package airlock.agent.graph.types;

import airlock.agent.graph.types.content.GraphContent;
import airlock.types.ShipName;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Post {
	public final String author;
	public final String index;
	@SerializedName("time-sent")
	public final long timeSent;
	public final List<GraphContent> contents;
	public final @Nullable String hash;
	public final List<String> signatures; // todo narrow by creating signature type

	public Post(String author, String index, long timeSent, List<GraphContent> contents, @Nullable String hash, List<String> signatures) {
		this.author = ShipName.withSig(author);
		this.index = index;
		this.timeSent = timeSent;
		this.contents = contents;
		this.hash = hash;
		this.signatures = signatures;
	}

	public Post(String author, String index, long timeSent, List<GraphContent> contents) {
		this(author, index, timeSent, contents, null, Collections.emptyList());
	}

	@Override
	public String toString() {
		return "Post{" +
				"author='" + author + '\'' +
				", index='" + index + '\'' +
				", timeSent=" + timeSent +
				", contents=" + contents +
				", hash='" + hash + '\'' +
				", signatures=" + signatures +
				'}';
	}
}
