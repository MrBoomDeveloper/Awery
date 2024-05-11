package com.mrboomdev.awery.extensions.request;

import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;

public class PostMediaCommentRequest {
	private CatalogComment comment, parent;
	private CatalogEpisode episode;

	public PostMediaCommentRequest setComment(CatalogComment comment) {
		this.comment = comment;
		return this;
	}

	public PostMediaCommentRequest setParentComment(CatalogComment parent) {
		this.parent = parent;
		return this;
	}

	public PostMediaCommentRequest setEpisode(CatalogEpisode episode) {
		this.episode = episode;
		return this;
	}

	public CatalogComment getComment() {
		return comment;
	}

	public CatalogComment getParentComment() {
		return parent;
	}

	public CatalogEpisode getEpisode() {
		return episode;
	}
}