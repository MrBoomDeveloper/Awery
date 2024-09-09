package com.mrboomdev.awery.util.exceptions;

public class ExtensionComponentMissingException extends Exception {
	private final String extensionName, componentName;

	public ExtensionComponentMissingException(String extensionName, String componentName) {
		super();
		this.extensionName = extensionName;
		this.componentName = componentName;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public String getComponentName() {
		return componentName;
	}
}