package com.mrboomdev.awery.catalog.template;

import java.util.List;
public class CatalogEpisode {
	private final String title, banner, description, url;
	private final float number;
	private final long releaseDate;
	private List<CatalogVideo> videos;

	public CatalogEpisode(String title, String url, String banner, String description, long releaseDate, float number) {
		this.title = title;
		this.url = url;
		this.number = number;
		this.banner = banner;
		this.description = description;
		this.releaseDate = releaseDate;
	}

	public void setVideos(List<CatalogVideo> videos) {
		this.videos = videos;
	}

	public List<CatalogVideo> getVideos() {
		return videos;
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