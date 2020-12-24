package airlock;

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

	// response.body() is a one-shot obj that needs to be copied manually, and we can't call it multiple times to use in multiple places
	// For example, we cannot inspect the body in one method, then pass the same response object to somewhere else because the buffer will be exhausted
	// this is why InMemoryResponseWrapper exists

	public InMemoryResponseWrapper(Response response) {
		this.closedResponse = response;
		// taken from https://github.com/square/okhttp/issues/2869
		BufferedSource source = Objects.requireNonNull(response.body(), "Got null response body").source();
		try {
			source.request(Integer.MAX_VALUE);
		} catch (IOException e) {
			// for now, we will fail fatally if we cannot buffer the body
			// why? because normally we are able to buffer the body even if it is empty and we are good
			// besides that, we often need to parse json directly from the body of a request such as in the case of a spider / scry request.
			// so, if we are unable to, we cannot really continue in any meaningful way (at least not that I know how to yet)
			// so we just conk out. this also makes the exception throwing cleaner in Urbit.java
			// until we have a proper way of addressing this, im leaving it as a runtime exception
			throw new RuntimeException("Unable to buffer body from okhttp response", e);
		}
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
