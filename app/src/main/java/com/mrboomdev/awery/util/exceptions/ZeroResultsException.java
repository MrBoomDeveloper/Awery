package com.mrboomdev.awery.util.exceptions;

/**
 * Throw it if after some fetch the result is empty
 */
public class ZeroResultsException extends RuntimeException {

	public ZeroResultsException() {
		super("Zero results were found!");
	}

	public ZeroResultsException(String message) {
		super(message);
	}

	public ZeroResultsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZeroResultsException(Throwable cause) {
		super(cause);
	}
}