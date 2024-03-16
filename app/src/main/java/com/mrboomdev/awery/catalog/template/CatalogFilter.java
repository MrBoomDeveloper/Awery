package com.mrboomdev.awery.catalog.template;

public class CatalogFilter {
	private final String query;
	private final int page;

	public CatalogFilter(String query, int page) {
		this.query = query;
		this.page = page;
	}

	public String getQuery() {
		return query;
	}

	public int getPage() {
		return page;
	}

	public static class Builder {
		private String query;
		private int page;

		public Builder setQuery(String query) {
			this.query = query;
			return this;
		}

		public Builder setPage(int page) {
			this.page = page;
			return this;
		}

		public String getQuery() {
			return query;
		}

		public int getPage() {
			return page;
		}

		public CatalogFilter build() {
			return new CatalogFilter(query, page);
		}
	}
}