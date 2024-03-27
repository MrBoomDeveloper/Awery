package com.mrboomdev.awery.extensions.support.template;

import com.squareup.moshi.Json;

import java.util.List;

public class CatalogComment {
	@Json(name = "author_name")
	public String authorName;
	@Json(name = "author_avatar")
	public String authorAvatar;
	public String text;
	public List<CatalogComment> items;
	@Json(name = "has_next_page")
	public boolean hasNextPage = false;
	@Json(name = "can_comment")
	public boolean canComment = false;
}