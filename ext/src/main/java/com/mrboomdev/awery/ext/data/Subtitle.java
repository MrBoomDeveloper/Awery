package com.mrboomdev.awery.ext.data;

import java.io.Serial;
import java.io.Serializable;

public class Subtitle implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private final String title, url;
	private String extra;

	public Subtitle(String url) {
		this.url = url;
		this.title = url;
	}

	public Subtitle(String title, String url) {
		this.title = title;
		this.url = url;
	}

	public Subtitle(String title, String url, String extra) {
		this.title = title;
		this.url = url;
		this.extra = extra;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getExtra() {
		return extra;
	}
}