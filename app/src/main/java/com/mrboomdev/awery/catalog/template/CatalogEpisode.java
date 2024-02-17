package com.mrboomdev.awery.catalog.template;

public class CatalogEpisode {
	private final String title, banner, description, url;
	private final float number;
	private final long releaseDate;

	public CatalogEpisode(String title, String url, String banner, String description, long releaseDate, float number) {
		this.title = title;
		this.url = url;
		this.number = number;
		this.banner = banner;
		this.description = description;
		this.releaseDate = releaseDate;
	}

	public long getReleaseDate() {
		return releaseDate;
	}

	public String getBanner() {
		return banner;
	}

	public String getDescription() {
		return description;
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