package com.mrboomdev.awery.util.exceptions;

public class TimeoutException extends Exception {

	public TimeoutException(String name) {
		super(name);
	}

	public TimeoutException(Throwable t) {
		super(t);
	}

	public TimeoutException(String name, Throwable t) {
		super(name, t);
	}

	public TimeoutException() {
		super();
	}
}