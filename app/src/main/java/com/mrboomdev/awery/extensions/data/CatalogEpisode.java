package com.mrboomdev.awery.extensions.data;

import androidx.annotation.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class CatalogEpisode implements Serializable, Comparable<CatalogEpisode> {
	@Serial
	private static final long serialVersionUID = 1;
	protected String title, banner, description, url;
	protected float number;
	protected final long releaseDate;
	protected List<CatalogVideo> videos;
	protected long id;

	public CatalogEpisode(String title, String url, String banner, String description, long releaseDate, float number) {
		this.title = title;
		this.url = url;
		this.number = number;
		this.banner = banner;
		this.description = description;
		this.releaseDate = releaseDate;
	}

	public CatalogEpisode(float number) {
		this(null, null, null, null, 0, number);
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

	public void setBanner(String banner) {
		this.banner = banner;
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

	public void setNumber(float number) {
		this.number = number;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public int compareTo(@NonNull CatalogEpisode o) {
		if(o.getNumber() != getNumber()) {
			return Float.compare(o.getNumber(), getNumber());
		}

		return o.getTitle().compareToIgnoreCase(getTitle());
	}
}