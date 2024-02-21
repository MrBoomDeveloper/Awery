package com.mrboomdev.awery.catalog.extensions;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.util.ErrorUtil;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class ExtensionProvider {

	public void search(SearchParams params, @NonNull ResponseCallback<List<CatalogMedia>> callback) {
		callback.onFailure(ErrorUtil.NOT_IMPLEMENTED);
	}

	public record SearchParams(Integer page, String query) {

		public static class Builder {
			private String query;
			private Integer page;

			public Builder setPage(Integer page) {
				this.page = page;
				return this;
			}

			public Builder setQuery(String query) {
				this.query = query;
				return this;
			}

			public String getQuery() {
				return query;
			}

			public Integer getPage() {
				return page;
			}

			public SearchParams build() {
				return new SearchParams(page, query);
			}
		}
	}

	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<CatalogEpisode>> callback) {
		callback.onFailure(ErrorUtil.NOT_IMPLEMENTED);
	}

	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		callback.onFailure(ErrorUtil.NOT_IMPLEMENTED);
	}

	public abstract String getName();

	public String getLang() {
		return "en";
	}

	public void getCatalogCategories(@NonNull ResponseCallback<Map<String, CatalogCategory>> callback) {
		callback.onFailure(ErrorUtil.NOT_IMPLEMENTED);
	}

	public interface ResponseCallback<T> {
		void onSuccess(T t);
		void onFailure(Throwable e);
	}
}