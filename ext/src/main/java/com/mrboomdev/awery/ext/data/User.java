package com.mrboomdev.awery.ext.data;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private String name, avatar, url, extra, role;
	private Integer lvl;
	private String[] flags;

	public String getName() {
		return name;
	}

	@Nullable
	public String[] getFlags() {
		return flags;
	}

	@Nullable
	public String getAvatar() {
		return avatar;
	}

	@Nullable
	public Integer getLevel() {
		return lvl;
	}

	@Nullable
	public String getRole() {
		return role;
	}

	@Nullable
	public String getExtra() {
		return extra;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	public static class Builder {
		private final User user = new User();

		public Builder setName(String name) {
			user.name = name;
			return this;
		}

		public Builder setUrl(String url) {
			user.url = url;
			return this;
		}

		public Builder setExtra(String extra) {
			user.extra = extra;
			return this;
		}

		public Builder setFlags(String... flags) {
			user.flags = flags;
			return this;
		}

		public Builder setAvatar(String avatar) {
			user.avatar = avatar;
			return this;
		}

		public Builder setRole(String role) {
			user.role = role;
			return this;
		}

		public Builder setLevel(Integer level) {
			user.lvl = level;
			return this;
		}

		public User build() {
			return user;
		}
	}
}