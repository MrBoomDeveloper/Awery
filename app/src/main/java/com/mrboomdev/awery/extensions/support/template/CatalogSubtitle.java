package com.mrboomdev.awery.extensions.support.template;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class CatalogSubtitle implements Parcelable {
	private final String title, url;

	public CatalogSubtitle(String title, String url) {
		this.title = title;
		this.url = url;
	}

	protected CatalogSubtitle(@NonNull Parcel in) {
		title = in.readString();
		url = in.readString();
	}

	public static final Creator<CatalogSubtitle> CREATOR = new Creator<>() {
		@NonNull
		@Contract("_ -> new")
		@Override
		public CatalogSubtitle createFromParcel(Parcel in) {
			return new CatalogSubtitle(in);
		}

		@NonNull
		@Contract(value = "_ -> new", pure = true)
		@Override
		public CatalogSubtitle[] newArray(int size) {
			return new CatalogSubtitle[size];
		}
	};

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(url);
	}
}