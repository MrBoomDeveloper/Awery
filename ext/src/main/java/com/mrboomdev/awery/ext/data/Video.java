package com.mrboomdev.awery.ext.data;

import java.io.Serial;
import java.io.Serializable;

public class Video implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private final String title, url, extra;

	public Video(String title, String url, String extra) {
		this.title = title;
		this.url = url;
		this.extra = extra;
	}

	public Video(String title, String url) {
		this(title, url, null);
	}

	public Video(String url) {
		this(url, url);
	}

	public String getExtra() {
		return extra;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
}