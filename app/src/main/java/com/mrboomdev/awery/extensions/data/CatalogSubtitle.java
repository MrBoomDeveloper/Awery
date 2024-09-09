package com.mrboomdev.awery.extensions.data;

import android.net.Uri;

import java.io.Serial;
import java.io.Serializable;

@Deprecated(forRemoval = true)
public class CatalogSubtitle implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private final String title, url;

	public CatalogSubtitle(String title, String url) {
		this.title = title;
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public Uri getUri() {
		return Uri.parse(getUrl());
	}
}