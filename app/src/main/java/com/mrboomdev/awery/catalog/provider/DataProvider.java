package com.mrboomdev.awery.catalog.provider;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogMedia;

import java.util.List;
import java.util.Map;

public abstract class DataProvider {
	public static final Throwable NOT_IMPLEMENTED = new UnsupportedOperationException("Not implemented!");

	public ProviderInfo getProviderInfo() {
		return ProviderInfo.UNKNOWN_PROVIDER_INFO;
	}

	public void search(int page, @NonNull OnSearchResponse onSearchResponse) {
		onSearchResponse.onFailed(NOT_IMPLEMENTED);
	}

	public Map<String, CatalogCategory> getCatalogCategories() {
		return Map.of();
	}

	public interface OnSearchResponse {
		void onLoaded(List<CatalogMedia<?>> media);

		void onFailed(Throwable t);
	}
}