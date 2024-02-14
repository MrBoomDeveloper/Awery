package com.mrboomdev.awery.catalog.provider.data;

import com.mrboomdev.awery.catalog.template.CatalogMedia;

public class Episode {
	private final String title;
	private final String url;
	private final float number;
	private final CatalogMedia media;

	public Episode(CatalogMedia media, String title, String url, float number) {
		this.title = title;
		this.url = url;
		this.number = number;
		this.media = media;
	}

	public CatalogMedia getMedia() {
		return media;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public float getNumber() {
		return number;
	}
}