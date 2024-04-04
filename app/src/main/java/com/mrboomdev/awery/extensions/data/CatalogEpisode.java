package com.mrboomdev.awery.extensions.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.List;

public class CatalogEpisode implements Parcelable, Comparable<CatalogEpisode> {
	private final String title, banner, description, url;
	private final float number;
	private final long releaseDate;
	private List<CatalogVideo> videos;
	private long id;

	public CatalogEpisode(String title, String url, String banner, String description, long releaseDate, float number) {
		this.title = title;
		this.url = url;
		this.number = number;
		this.banner = banner;
		this.description = description;
		this.releaseDate = releaseDate;
	}

	protected CatalogEpisode(@NonNull Parcel in) {
		title = in.readString();
		banner = in.readString();
		description = in.readString();
		url = in.readString();
		number = in.readFloat();
		releaseDate = in.readLong();
	}

	public static final Creator<CatalogEpisode> CREATOR = new Creator<>() {
		@NonNull
		@Contract("_ -> new")
		@Override
		public CatalogEpisode createFromParcel(Parcel in) {
			return new CatalogEpisode(in);
		}

		@NonNull
		@Contract(value = "_ -> new", pure = true)
		@Override
		public CatalogEpisode[] newArray(int size) {
			return new CatalogEpisode[size];
		}
	};

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

	@Override
	public int describeContents() {
		return 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(banner);
		dest.writeString(description);
		dest.writeString(url);
		dest.writeFloat(number);
		dest.writeLong(releaseDate);
	}

	@Override
	public int compareTo(@NonNull CatalogEpisode o) {
		if(o.getNumber() != getNumber()) {
			return Float.compare(o.getNumber(), getNumber());
		}

		return o.getTitle().compareToIgnoreCase(getTitle());
	}
}