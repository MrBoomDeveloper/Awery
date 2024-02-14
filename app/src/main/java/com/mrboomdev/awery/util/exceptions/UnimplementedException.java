package com.mrboomdev.awery.util.exceptions;

public class UnimplementedException extends Exception {

	public UnimplementedException(String name) {
		super(name);
	}

	public UnimplementedException(Throwable t) {
		super(t);
	}

	public UnimplementedException(String name, Throwable t) {
		super(name, t);
	}

	public UnimplementedException() {
		super();
	}
}