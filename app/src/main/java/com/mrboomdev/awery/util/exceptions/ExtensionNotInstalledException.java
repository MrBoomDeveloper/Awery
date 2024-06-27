package com.mrboomdev.awery.util.exceptions;

public class ExtensionNotInstalledException extends Exception {

	public ExtensionNotInstalledException() {
		super();
	}

	public ExtensionNotInstalledException(String message) {
		super(message);
	}

	public ExtensionNotInstalledException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtensionNotInstalledException(Throwable cause) {
		super(cause);
	}
}