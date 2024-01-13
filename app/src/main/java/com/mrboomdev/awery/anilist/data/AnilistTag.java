package com.mrboomdev.awery.anilist.data;

import androidx.annotation.NonNull;
public class AnilistTag {
	private final String name, description, category;
	private final boolean isAdult;

	public AnilistTag(String name, String description, String category, boolean isAdult) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.isAdult = isAdult;
	}

	public AnilistTag(String name, boolean isAdult) {
		this(name, null, null, isAdult);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public boolean isAdult() {
		return isAdult;
	}

	@NonNull
	@Override
	public String toString() {
		return name;
	}
}