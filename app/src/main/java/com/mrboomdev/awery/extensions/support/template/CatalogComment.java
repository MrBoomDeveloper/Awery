package com.mrboomdev.awery.extensions.support.template;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

public class CatalogComment {
	public static final int HIDDEN = -1;
	public static final int DISABLED = -2;
	@Json(name = "author_name")
	public String authorName;
	@Json(name = "author_avatar")
	public String authorAvatar;
	public String text;
	public List<CatalogComment> items = new ArrayList<>();
	@Json(name = "has_next_page")
	public boolean hasNextPage = false;
	@Json(name = "can_comment")
	public boolean canComment = false;
	/**
	 * If this value equal to -1, then likes aren't shown
	 * If -2, then you can't use them
	 */
	@Json(name = "likes")
	public int likes;
	/**
	 * If this value equal to -1, then dislikes aren't shown
	 * If -2, then you can't use them
	 */
	@Json(name = "dislikes")
	public int dislikes;
	/**
	 * If this value equal to -1, then votes aren't shown
	 */
	@Json(name = "votes")
	public int votes;
	@Json(ignore = true)
	public long visualId;
}