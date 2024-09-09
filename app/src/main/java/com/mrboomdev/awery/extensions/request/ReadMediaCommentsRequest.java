package com.mrboomdev.awery.extensions.request;

import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogMedia;

@Deprecated(forRemoval = true)
public class ReadMediaCommentsRequest {
	private CatalogMedia media;
	private CatalogVideo episode;
	private CatalogComment parentComment;
	private String sortMode;
	private int page;

	public ReadMediaCommentsRequest setMedia(CatalogMedia media) {
		this.media = media;
		return this;
	}

	public ReadMediaCommentsRequest setEpisode(CatalogVideo episode) {
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

	public CatalogMedia getMedia() {
		return media;
	}

	public CatalogVideo getEpisode() {
		return episode;
	}

	public CatalogComment getParentComment() {
		return parentComment;
	}

	public String getSortMode() {
		return sortMode;
	}

	public int getPage() {
		return page;
	}
}