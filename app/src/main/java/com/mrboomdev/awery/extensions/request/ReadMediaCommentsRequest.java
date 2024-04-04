package com.mrboomdev.awery.extensions.request;

import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogMedia;

import org.mozilla.javascript.annotations.JSGetter;

public class ReadMediaCommentsRequest {
	private CatalogMedia media;
	private CatalogEpisode episode;
	private CatalogComment parentComment;
	private String sortMode;
	private int page;

	public ReadMediaCommentsRequest setMedia(CatalogMedia media) {
		this.media = media;
		return this;
	}

	public ReadMediaCommentsRequest setEpisode(CatalogEpisode episode) {
		this.episode = episode;
		return this;
	}

	public ReadMediaCommentsRequest setParentComment(CatalogComment parentComment) {
		this.parentComment = parentComment;
		return this;
	}

	public ReadMediaCommentsRequest setSortMode(String sortMode) {
		this.sortMode = sortMode;
		return this;
	}

	public ReadMediaCommentsRequest setPage(int page) {
		this.page = page;
		return this;
	}

	@JSGetter("media")
	public CatalogMedia getMedia() {
		return media;
	}

	@JSGetter("episode")
	public CatalogEpisode getEpisode() {
		return episode;
	}

	@JSGetter("parentComment")
	public CatalogComment getParentComment() {
		return parentComment;
	}

	@JSGetter("sortMode")
	public String getSortMode() {
		return sortMode;
	}

	@JSGetter("page")
	public int getPage() {
		return page;
	}
}