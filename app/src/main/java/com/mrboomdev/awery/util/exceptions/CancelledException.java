package com.mrboomdev.awery.util.exceptions;

public class CancelledException extends RuntimeException {

	public CancelledException() {
		super();
	}

	public CancelledException(String message) {
		super(message);
	}

	public CancelledException(String message, Throwable cause) {
		super(message, cause);
	}

	public CancelledException(Throwable cause) {
		super(cause);
	}
}