package com.mrboomdev.awery.catalog.provider;

public class ProviderInfo {
	public String title, version, author;

	public static final ProviderInfo UNKNOWN_PROVIDER_INFO = new ProviderInfo() {{
		title = "Unknown";
		version = "Unknown";
		author = "Unknown";
	}};
}