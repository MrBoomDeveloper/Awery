package com.mrboomdev.awery.ext.data;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

public class Episode implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private String title, description, thumbnail, extra, url;
	private float number;
	private String[] flags;

	public String getTitle() {
		return title;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	@Nullable
	public String[] getFlags() {
		return flags;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@Nullable
	public String getThumbnail() {
		return thumbnail;
	}

	@Nullable
	public Float getNumber() {
		return number;
	}

	@Nullable
	public String getExtra() {
		return extra;
	}

	public static class Builder {
		private final Episode episode = new Episode();

		public Builder setTitle(String title) {
			episode.title = title;
			return this;
		}

		public Builder setDescription(String description) {
			episode.description = description;
			return this;
		}

		public Builder setThumbnail(String thumbnail) {
			episode.thumbnail = thumbnail;
			return this;
		}

		public Builder setFlags(String[] flags) {
			episode.flags = flags;
			return this;
		}

		public Builder setUrl(String url) {
			episode.url = url;
			return this;
		}

		public Builder setExtra(String extra) {
			episode.extra = extra;
			return this;
		}

		public Builder setNumber(float number) {
			episode.number = number;
			return this;
		}

		public Episode build() {
			return episode;
		}
	}
}