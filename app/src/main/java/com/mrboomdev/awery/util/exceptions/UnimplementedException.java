package com.mrboomdev.awery.util.exceptions;

/**
 * You can throw this exception for example inside of some default method of the interface to mark that this particular
 * class doesn't support such functionality.
 * @author MrBoomDev
 */
public class UnimplementedException extends RuntimeException {

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