package com.mrboomdev.awery.extensions.data;

// TODO: 10/14/2024 Move this class to the database package
public class CatalogList {
	private String title;
	private final String id;

	public CatalogList(String title, String id) {
		this.title = title;
		this.id = id;
	}

	public CatalogList(String title) {
		this.title = title;
		this.id = String.valueOf(System.currentTimeMillis());
	}

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}