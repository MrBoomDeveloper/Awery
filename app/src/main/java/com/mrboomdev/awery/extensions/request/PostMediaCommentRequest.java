package com.mrboomdev.awery.extensions.request;

import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogVideo;

public class PostMediaCommentRequest {
	private CatalogComment comment, parent;
	private CatalogVideo episode;

	public PostMediaCommentRequest setComment(CatalogComment comment) {
		this.comment = comment;
		return this;
	}

	public PostMediaCommentRequest setParentComment(CatalogComment parent) {
		this.parent = parent;
		return this;
	}

	public PostMediaCommentRequest setEpisode(CatalogVideo episode) {
		this.episode = episode;
		return this;
	}

	public CatalogComment getComment() {
		return comment;
	}

	public CatalogComment getParentComment() {
		return parent;
	}

	public CatalogVideo getEpisode() {
		return episode;
	}
}