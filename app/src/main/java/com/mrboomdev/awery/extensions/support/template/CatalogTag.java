package com.mrboomdev.awery.extensions.support.template;

public class CatalogTag {
	private String name, id, description;
	private boolean isAdult, isSpoiler;

	public CatalogTag(String name) {
		this.name = name;
		this.id = name;
	}

	public CatalogTag() {}

	public CatalogTag(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIsAdult(boolean adult) {
		isAdult = adult;
	}

	public void setIsSpoiler(boolean spoiler) {
		isSpoiler = spoiler;
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

	public boolean isSpoiler() {
		return isSpoiler;
	}
}