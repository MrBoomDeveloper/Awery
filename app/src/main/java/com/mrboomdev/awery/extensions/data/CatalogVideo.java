package com.mrboomdev.awery.extensions.data;

import androidx.annotation.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Deprecated(forRemoval = true)
public class CatalogVideo implements Serializable, Comparable<CatalogVideo> {
	@Serial
	private static final long serialVersionUID = 1;
	protected String title, banner, description, url;
	protected float number;
	protected final long releaseDate;
	protected List<CatalogVideoFile> videos;
	protected long id;

	public CatalogVideo(String title, String url, String banner, String description, long releaseDate, float number) {
		this.title = title;
		this.url = url;
		this.number = number;
		this.banner = banner;
		this.description = description;
		this.releaseDate = releaseDate;
	}

	public CatalogVideo(float number) {
		this(null, null, null, null, 0, number);
	}

	public void setVideos(List<CatalogVideoFile> videos) {
		this.videos = videos;
	}

	public List<CatalogVideoFile> getVideos() {
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
	public int compareTo(@NonNull CatalogVideo o) {
		if(o.getNumber() != getNumber()) {
			return Float.compare(o.getNumber(), getNumber());
		}

		return o.getTitle().compareToIgnoreCase(getTitle());
	}
}