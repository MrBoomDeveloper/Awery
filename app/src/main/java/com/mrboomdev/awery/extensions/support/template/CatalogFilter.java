package com.mrboomdev.awery.extensions.support.template;

import java.util.List;

public class CatalogFilter {
	protected List<String> includedTags, excludedTags;
	protected Long startDate, endDate;
	protected String query;
	protected int page;

	public String getQuery() {
		return query;
	}

	public int getPage() {
		return page;
	}

	public List<String> getIncludedTags() {
		return includedTags;
	}

	public List<String> getExcludedTags() {
		return excludedTags;
	}

	public Long getStartDate() {
		return startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public static class Builder {
		private List<String> includedTags, excludedTags;
		private long startDate, endDate;
		private String query;
		private int page;

		public Builder setQuery(String query) {
			this.query = query;
			return this;
		}

		public Builder setStartDate(long startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder setEndDate(long endDate) {
			this.endDate = endDate;
			return this;
		}

		public long getStartDate() {
			return startDate;
		}

		public long getEndDate() {
			return endDate;
		}

		public Builder setIncludedTags(List<String> includedTags) {
			this.includedTags = includedTags;
			return this;
		}

		public Builder setExcludedTags(List<String> excludedTags) {
			this.excludedTags = excludedTags;
			return this;
		}

		public List<String> getIncludedTags() {
			return includedTags;
		}

		public List<String> getExcludedTags() {
			return excludedTags;
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
			var result = new CatalogFilter();
			result.query = query;
			result.includedTags = includedTags;
			result.excludedTags = excludedTags;
			result.page = page;
			result.startDate = startDate;
			result.endDate = endDate;
			return result;
		}
	}
}