package airlock.agent.graph.types;

import airlock.agent.graph.types.content.GraphContent;
import airlock.types.ShipName;
import com.google.gson.annotations.SerializedName;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

// (move this into a better place later)
// design note: the reason for the `@With` annotation is to do the following use case
// i want to be able to keep the data class immutable (i.e. all original data to/from urbit)
// but i still want to be able to "clone" it (i.e., make a copy / derive a `Post` object from an existing one)
// without modifying the original object.
// i looked at a number of options, and settled on the `@With` strategy for now, but heres what i looiekd at
// 1. copy constructor
//   this is basically where you have a constructor that takes in itself, and generates a copy.
// 2. manually generated builder, lombok `@Builder`
//     todo explain the builder pattern and why i didnt go with it (generated was too boiler platey, and i still wanted a constructor feel rather than a builder. `set$Property`
// 3. some dude said that you shouldn't have this problem because it means that you have an 'anemic data model' or something
// idk and it is a design smell but like idk man screw that.
// http://gregorriegler.com/2019/08/10/who-needs-lombok-anyhow.html
// it was also hard to decide what to keep mutable and what to keep immutable
// for example, i ended up keeping graph mutable because im not about to have 15 deep-copies of a freaking graph floating around
// but at the same time it makes a lot of sense to make `Post` and `Node` mostly immutable
@With
public class Post {
	// todo make author final somehow?? please???
	public String author;
	public final String index; // can be indexList i.e. List<BigInteger>
	@SerializedName("time-sent")
	public final long timeSent;
	public final List<GraphContent> contents;
	public final @Nullable String hash;
	public final List<String> signatures; // todo narrow by creating signature type

	// strictly part of landscape, not part of urbit, so not received in serialized object
	// only modified by `GraphAgent.markPending`
	private boolean pending;

	private Post(String author, String index, long timeSent, List<GraphContent> contents, @Nullable String hash, List<String> signatures, boolean pending) {
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
		this.pending = pending;
	}


	public Post(String author, String index, long timeSent, List<GraphContent> contents, @Nullable String hash, List<String> signatures) {
		this(author, index, timeSent, contents, hash, signatures, false); // by default, posts are not pending
	}

	public Post(String author, String index, long timeSent, List<GraphContent> contents) {
		this(author, index, timeSent, contents, null, Collections.emptyList()); // no hash, and no signatures
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
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
