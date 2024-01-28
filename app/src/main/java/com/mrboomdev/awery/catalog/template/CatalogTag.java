package com.mrboomdev.awery.catalog.template;

public class CatalogTag {
	public String name, id, description;
	public boolean isAdult, isSpoiler;

	public CatalogTag(String name, String description, boolean isAdult) {
		this.name = name;
		this.description = description;
		this.isAdult = isAdult;
	}

	public CatalogTag() {}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isAdult() {
		return isAdult;
	}
}