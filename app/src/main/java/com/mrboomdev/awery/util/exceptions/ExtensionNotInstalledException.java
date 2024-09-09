package com.mrboomdev.awery.util.exceptions;

public class ExtensionNotInstalledException extends Exception {
	private final String extensionName;

	public ExtensionNotInstalledException(Throwable cause) {
		super(cause);
		this.extensionName = null;
	}

	public ExtensionNotInstalledException(String extensionName) {
		super("Extension \"" + extensionName + "\" has failed to load!");
		this.extensionName = extensionName;
	}

	public ExtensionNotInstalledException(String extensionName, String message) {
		super(message);
		this.extensionName = extensionName;
	}

	public ExtensionNotInstalledException(String extensionName, Throwable cause) {
		super("Extension not installed! " + extensionName, cause);
		this.extensionName = extensionName;
	}

	public String getExtensionName() {
		return extensionName;
	}
}