package airlock.errors.channel;

import airlock.InMemoryResponseWrapper;

/**
 * Thrown when there was a generic problem with the response gotten from the ship over the airlock channel
 */
public class AirlockResponseError extends AirlockChannelError {

	// this might be a code smell on how complex the exception handling is and how i'm having to include it in an error but oh well
	public final InMemoryResponseWrapper responseWrapper;
	public AirlockResponseError(String message, InMemoryResponseWrapper responseWrapper) {
		super(message);
		this.responseWrapper = responseWrapper;
	}

	public AirlockResponseError(String message, InMemoryResponseWrapper responseWrapper, Throwable cause) {
		super(message, cause);
		this.responseWrapper = responseWrapper;
	}

	public AirlockResponseError(InMemoryResponseWrapper responseWrapper, Throwable cause) {
		super(cause);
		this.responseWrapper = responseWrapper;
	}
}
