package com.mrboomdev.awery.ext.data;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

public class ExternalService implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private String icon, title, url, extra;
	private float rating;

	public String getIcon() {
		return icon;
	}

	public String getTitle() {
		return title;
	}

	public float getRating() {
		return rating;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	@Nullable
	public String getExtra() {
		return extra;
	}

	public static class Builder {
		private final ExternalService rating = new ExternalService();

		public Builder setIcon(String icon) {
			rating.icon = icon;
			return this;
		}

		public Builder setTitle(String title) {
			rating.title = title;
			return this;
		}

		public Builder setUrl(String url) {
			rating.url = url;
			return this;
		}

		public Builder setRating(float rating) {
			this.rating.rating = rating;
			return this;
		}

		public Builder setExtra(String extra) {
			rating.extra = extra;
			return this;
		}

		public ExternalService build() {
			return rating;
		}
	}
}