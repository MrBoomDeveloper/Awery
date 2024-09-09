package com.mrboomdev.awery.ext.data;

import com.mrboomdev.awery.ext.constants.Awery;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

public class Comment implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private static JsonAdapter<Comment> adapter;
	private Collection<Comment> items;
	private String content, extra, url;
	private Collection<String> flags;
	private Integer likes, dislikes;
	private User user;
	private Long creationDate, editDate;

	@Override
	public String toString() {
		if(adapter == null) {
			adapter = new Moshi.Builder().build().adapter(Comment.class);
		}

		return adapter.toJson(this);
	}

	public String getUrl() {
		return url;
	}

	public String getExtra() {
		return extra;
	}

	public String getContent() {
		return content;
	}

	@Nullable
	public Collection<String> getFlags() {
		return flags;
	}

	@Nullable
	public Collection<Comment> getItems() {
		return items;
	}

	public User getUser() {
		return user;
	}

	public Integer getLikes() {
		return likes;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public Long getUpdateDate() {
		return editDate;
	}

	public Integer getDislikes() {
		return dislikes;
	}

	public static class Builder {
		private final Comment comment = new Comment();

		public Builder setContent(String content) {
			comment.content = content;
			return this;
		}

		public Builder setLikes(Integer likes) {
			comment.likes = likes;
			return this;
		}

		public Builder setDislikes(Integer dislikes) {
			comment.dislikes = dislikes;
			return this;
		}

		public Builder setItems(Collection<Comment> comments) {
			comment.items = comments;
			return this;
		}

		public Builder setUser(User user) {
			comment.user = user;
			return this;
		}

		public Builder setUrl(String url) {
			comment.url = url;
			return this;
		}

		public Builder setExtra(String extra) {
			comment.extra = extra;
			return this;
		}

		public Builder setFlags(Collection<String> flags) {
			comment.flags = flags;
			return this;
		}

		public Builder setCreationDate(Long creationDate) {
			comment.creationDate = creationDate;
			return this;
		}

		public Builder setEditDate(Long editDate) {
			comment.editDate = editDate;
			return this;
		}

		public Builder setIsEdited(boolean isEdited) {
			comment.editDate = isEdited ? Awery.EDITED_WITHOUT_DATE : null;
			return this;
		}

		public Comment build() {
			return comment;
		}
	}
}