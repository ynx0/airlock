import okhttp3.Response;
import okio.BufferedSource;
import okio.ByteString;

import java.io.IOException;
import java.util.Objects;


/**
 * This class wraps around an {@link okhttp3.Response} object providing an immutable version of it
 * <p>
 *     Specifically, it makes generates an immutable "copy" of the response body.
 * </p>
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
		// taken from https://github.com/square/okhttp/issues/2869
		BufferedSource source = Objects.requireNonNull(response.body()).source();
		source.request(Integer.MAX_VALUE);
		this.inMemoryResponseBody = source.getBuffer().snapshot();
		response.close();
	}

	/**
	 * Get the underlying response object
	 * @return the underlying response object
	 */
	public Response getClosedResponse() {
		return this.closedResponse;
	}

	/**
	 * Get the immutable copy of the response body as a {@link ByteString}
	 * @return the response body
	 */
	public ByteString getBody() {
		// todo, returning a OkHttp.ByteString requires the outside user to also depend on okhttp
		//  which may be bad as it is leaking unneeded apis. so, maybe we should return a ByteBuffer (native java thingy) instead
		return this.inMemoryResponseBody;
	}

}
