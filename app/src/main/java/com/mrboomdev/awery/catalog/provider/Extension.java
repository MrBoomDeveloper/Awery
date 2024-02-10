package com.mrboomdev.awery.catalog.provider;

public class Extension {
	private final boolean isNsfw;
	private final String version, id;
	private final String name;
	private String error;
	private Exception exception;
	private ExtensionProvider provider;

	public Extension(String id, String name, boolean isNsfw, String version) {
		this.name = name;
		this.isNsfw = isNsfw;
		this.version = version;
		this.id = id;
	}

	public ExtensionProvider getProvider() {
		return provider;
	}

	public void setProvider(ExtensionProvider provider) {
		this.provider = provider;
	}

	public String getError() {
		return error;
	}

	public String getId() {
		return id;
	}

	public void setError(String error, Exception e) {
		this.error = error;
		this.exception = e;
	}

	public Exception getException() {
		return exception;
	}

	public void setError(String error) {
		this.error = error;

		if(error == null) {
			exception = null;
		}
	}

	public boolean isError() {
		return error != null || exception != null;
	}

	public String getName() {
		return name;
	}

	public boolean isNsfw() {
		return isNsfw;
	}

	public String getVersion() {
		return version;
	}
}