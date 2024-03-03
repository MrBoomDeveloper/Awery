package com.mrboomdev.awery.catalog.template;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class CatalogVideo implements Parcelable {
	private final String url, headers, title;

	public CatalogVideo(String title, String url, String headers) {
		this.title = title;
		this.url = url;
		this.headers = headers;
	}

	protected CatalogVideo(@NonNull Parcel in) {
		title = in.readString();
		url = in.readString();
		headers = in.readString();
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
	}
}