package com.mrboomdev.awery.util.exceptions;

public class HttpException extends RuntimeException {

	public HttpException(Throwable t) {
		super(t);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(String message, Throwable t) {
		super(message, t);
	}
}