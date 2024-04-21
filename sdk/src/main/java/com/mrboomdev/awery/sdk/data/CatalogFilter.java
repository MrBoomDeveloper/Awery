package com.mrboomdev.awery.sdk.data;

import java.util.List;

public class CatalogFilter {
	private List<String> includedTags, excludedTags;
	private Long startDate, endDate;
	private String query;
	private int page;

	public CatalogFilter setQuery(String query) {
		this.query = query;
		return this;
	}

	public CatalogFilter setStartDate(long startDate) {
		this.startDate = startDate;
		return this;
	}

	public CatalogFilter setEndDate(long endDate) {
		this.endDate = endDate;
		return this;
	}

	public long getStartDate() {
		return startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public CatalogFilter setIncludedTags(List<String> includedTags) {
		this.includedTags = includedTags;
		return this;
	}

	public CatalogFilter setExcludedTags(List<String> excludedTags) {
		this.excludedTags = excludedTags;
		return this;
	}

	public List<String> getIncludedTags() {
		return includedTags;
	}

	public List<String> getExcludedTags() {
		return excludedTags;
	}

	public CatalogFilter setPage(int page) {
		this.page = page;
		return this;
	}

	public String getQuery() {
		return query;
	}

	public int getPage() {
		return page;
	}
}