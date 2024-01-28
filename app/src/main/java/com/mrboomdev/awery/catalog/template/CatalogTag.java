package com.mrboomdev.awery.catalog.template;

public class CatalogTag {
	private final String name, description;
	private final boolean isAdult;

	public CatalogTag(String name, String description, boolean isAdult) {
		this.name = name;
		this.description = description;
		this.isAdult = isAdult;
	}

	public CatalogTag(String name, boolean isAdult) {
		this(name, null, isAdult);
	}

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