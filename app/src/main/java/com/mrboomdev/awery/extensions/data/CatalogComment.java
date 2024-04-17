package com.mrboomdev.awery.extensions.data;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

public class CatalogComment {
	public static final int HIDDEN = -1;
	public static final int DISABLED = -2;
	public static final int VOTE_STATE_LIKED = -3;
	public static final int VOTE_STATE_DISLIKED = -4;
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
	@Json(name = "comments")
	public int comments;
	/**
	 * If this value equal to -1, then votes aren't shown
	 */
	@Json(name = "votes")
	public Integer votes;
	public String date;

	/**
	 * Used only for the Frontend
	 * Do not use in the Backend!
	 * <p>Used to identify this comment among others</p>
	 */
	@Json(ignore = true)
	public long visualId;
	@Json(ignore = true)
	public int voteState;
}