package com.mrboomdev.awery.catalog.extensions;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogFilter;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.List;
import java.util.Map;

/**
 * Base class for all extension providers.
 * Please note that this class can be a ExtensionProviderGroup, which doesn't implement media source methods.
 * @author MrBoomDev
 */
@SuppressWarnings("unused")
public abstract class ExtensionProvider implements Comparable<ExtensionProvider> {

	@Override
	public int compareTo(@NonNull ExtensionProvider o) {
		if(getName().equals(o.getName())) {
			return getLang().compareToIgnoreCase(o.getLang());
		}

		return getName().compareToIgnoreCase(o.getName());
	}

	public void search(CatalogFilter filter, @NonNull ResponseCallback<List<CatalogMedia>> callback) {
		callback.onFailure(new UnimplementedException("Search not implemented!"));
	}

	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<CatalogEpisode>> callback) {
		callback.onFailure(new UnimplementedException("Episodes not implemented!"));
	}

	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		callback.onFailure(new UnimplementedException("Videos not implemented!"));
	}

	/**
	 * @return A human-readable name of the extension
	 * @author MrBoomDev
	 */
	public abstract String getName();

	/**
	 * @apiNote The returned value can be an array of format: "en;ru;jp"
	 * @return The language of the extension
	 * @author MrBoomDev
	 */
	public String getLang() {
		return "en";
	}

	public void getCatalogCategories(@NonNull ResponseCallback<Map<String, CatalogCategory>> callback) {
		callback.onFailure(new UnimplementedException("Categories not implemented!"));
	}

	@NonNull
	@Override
	public String toString() {
		return getName();
	}

	public interface ResponseCallback<T> {
		void onSuccess(T t);
		void onFailure(Throwable e);
	}
}