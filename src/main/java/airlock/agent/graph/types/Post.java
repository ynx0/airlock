package airlock.agent.graph.types;

import airlock.agent.graph.types.content.GraphContent;
import airlock.types.ShipName;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Post {
	public String author;
	public final String index;
	@SerializedName("time-sent")
	public final long timeSent;
	public final List<GraphContent> contents;
	public final @Nullable String hash;
	public final List<String> signatures; // todo narrow by creating signature type

	// strictly part of landscape, not part of urbit, so not received in serialized object
	// only modified by `GraphAgent.markPending`
	public boolean pending = false;

	public Post(String author, String index, long timeSent, List<GraphContent> contents, @Nullable String hash, List<String> signatures) {
		// okay, slight wtf moment but although I'm pretty sure
		// all posts are supposed to come with the ship having a sig,
		// the `api/graph.ts:markPending` function explicitly modifies a node's post's author
		// to be without a sig. so i am like uh wtf. maybe this is wrong???
		// and i don't know what it means for serialization either
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
