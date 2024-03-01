package com.mrboomdev.awery.util.exceptions;

public class InvalidSyntaxException extends RuntimeException {

	public InvalidSyntaxException(String message) {
		super(message);
	}

	public InvalidSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSyntaxException(Throwable cause) {
		super(cause);
	}
}