package com.mrboomdev.awery.catalog.template;

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