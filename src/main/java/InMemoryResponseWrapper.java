import okhttp3.Response;
import okio.BufferedSource;
import okio.ByteString;

import java.io.IOException;
import java.util.Objects;


/**
 * This class wraps around an {@link okhttp3.Response} object providing
 */
public class InMemoryResponseWrapper {
	// todo, maybe we don't need to keep around this data and the class is useless
	//  the Response body is meant to be thrown away as per okhttp design
	//  in the future, maybe we should remove the use of this class entirely and not return any responses.
	// for now im keeping it tho
	private final Response closedResponse;
	private final ByteString inMemoryResponseBody;

	public InMemoryResponseWrapper(Response response) throws IOException {
		this.closedResponse = response;
		BufferedSource source = Objects.requireNonNull(response.body()).source();
		source.request(Integer.MAX_VALUE);
		this.inMemoryResponseBody = source.getBuffer().snapshot();
		// todo figure out what we gotta do to properly close everything here
		response.close();
		System.out.println("body source closed?: " + !source.isOpen()); // true, so we don't need to manually close the source
	}

	public Response getClosedResponse() {
		return this.closedResponse;
	}

	public ByteString getBody() {
		return this.inMemoryResponseBody;
	}

}
