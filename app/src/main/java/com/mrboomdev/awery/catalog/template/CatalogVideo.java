package com.mrboomdev.awery.catalog.template;

public class CatalogVideo {
	private final String url, headers;

	public CatalogVideo(String url, String headers) {
		this.url = url;
		this.headers = headers;
	}

	public String getHeaders() {
		return headers;
	}

	public String getUrl() {
		return url;
	}
}