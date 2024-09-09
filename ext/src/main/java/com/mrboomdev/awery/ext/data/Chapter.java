package com.mrboomdev.awery.ext.data;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

public class Chapter implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private String title, extra, url;
	private String[] pages;

	public Chapter(String title, String[] pages, String extra) {
		this.title = title;
		this.pages = pages;
		this.extra = extra;
	}

	public Chapter(String title, String[] pages) {
		this(title, pages, null);
	}

	private Chapter() {}

	public String getTitle() {
		return title;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	@Nullable
	public String getExtra() {
		return extra;
	}

	@Nullable
	public String[] getPages() {
		return pages;
	}

	public static class Builder {
		private final Chapter chapter = new Chapter();

		public Builder setTitle(String title) {
			chapter.title = title;
			return this;
		}

		public Builder setUrl(String url) {
			chapter.url = url;
			return this;
		}

		public Builder setExtra(String extra) {
			chapter.extra = extra;
			return this;
		}

		public Builder setPages(String[] pages) {
			chapter.pages = pages;
			return this;
		}

		public Chapter build() {
			return chapter;
		}
	}
}