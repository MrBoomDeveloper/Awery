package com.mrboomdev.awery.extensions.support.template;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class CatalogVideo implements Parcelable {
	private final String url, headers, title;
	private List<CatalogSubtitle> subtitles;

	public CatalogVideo(String title, String url, String headers, List<CatalogSubtitle> subtitles) {
		this.title = title;
		this.url = url;
		this.headers = headers;
		this.subtitles = subtitles;
	}

	public CatalogVideo(String title, String url, String headers) {
		this.title = title;
		this.url = url;
		this.headers = headers;
	}

	public void setSubtitles(List<CatalogSubtitle> subtitles) {
		this.subtitles = subtitles;
	}

	public List<CatalogSubtitle> getSubtitles() {
		return subtitles;
	}

	protected CatalogVideo(@NonNull Parcel in) {
		title = in.readString();
		url = in.readString();
		headers = in.readString();

		if(in.readByte() != 0x00) {
			subtitles = new ArrayList<>();
			in.readList(subtitles, CatalogSubtitle.class.getClassLoader());
		} else {
			subtitles = null;
		}
	}

	public static final Creator<CatalogVideo> CREATOR = new Creator<>() {
		@NonNull
		@Contract("_ -> new")
		@Override
		public CatalogVideo createFromParcel(Parcel in) {
			return new CatalogVideo(in);
		}

		@NonNull
		@Contract(value = "_ -> new", pure = true)
		@Override
		public CatalogVideo[] newArray(int size) {
			return new CatalogVideo[size];
		}
	};

	public String getHeaders() {
		return headers;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(url);
		dest.writeString(headers);

		if(subtitles == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(subtitles);
		}
	}
}