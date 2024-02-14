package com.mrboomdev.awery.catalog.provider;

import java.util.ArrayList;
import java.util.List;

public class Extension {
	private final boolean isNsfw;
	private final String version, id;
	private final String name;
	private String error;
	protected boolean isVideoExtension, isBookExtension;
	private Exception exception;
	private final List<ExtensionProvider> providers = new ArrayList<>();

	public Extension(String id, String name, boolean isNsfw, String version) {
		this.name = name;
		this.isNsfw = isNsfw;
		this.version = version;
		this.id = id;
	}

	public List<ExtensionProvider> getProviders() {
		return providers;
	}

	public void addProvider(ExtensionProvider provider) {
		this.providers.add(provider);
	}

	public String getError() {
		return error;
	}

	public String getId() {
		return id;
	}

	public boolean isVideoExtension() {
		return isVideoExtension;
	}

	public boolean isBookExtension() {
		return isBookExtension;
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