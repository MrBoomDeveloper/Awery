package com.mrboomdev.awery.catalog.provider;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.provider.data.Episode;
import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class ExtensionProvider {
	private static final UnimplementedException NOT_IMPLEMENTED = new UnimplementedException("Not implemented!");
	public static final Collection<?> ZERO_RESULTS_LIST = List.of();
	public static final Collection<?> CONNECTION_FAILED_LIST = List.of();

	public void search(int page, @NonNull ResponseCallback<Collection<CatalogMedia>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<Collection<Episode>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public abstract String getName();

	public String getLang() {
		return "en";
	}

	public void getCatalogCategories(@NonNull ResponseCallback<Map<String, CatalogCategory>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public interface ResponseCallback<T> {
		void onSuccess(T t);
		void onFailure(Throwable e);
	}
}