package com.mrboomdev.awery.util.exceptions;

public class ExtensionNotInstalledException extends Exception {
	private final String extensionName;

	public ExtensionNotInstalledException(String extensionName) {
		super("Extension not installed! " + extensionName);
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