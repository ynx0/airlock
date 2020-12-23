package airlock.agent.graph;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class Node {

	final Post post;
	final @Nullable Graph children; // technically internal graph

	public Node(Post post, @Nullable Graph children) {
		this.post = post;
		this.children = children;
	}


}
