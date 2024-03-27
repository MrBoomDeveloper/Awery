package com.mrboomdev.awery.extensions;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.support.template.CatalogCategory;
import com.mrboomdev.awery.extensions.support.template.CatalogComment;
import com.mrboomdev.awery.extensions.support.template.CatalogEpisode;
import com.mrboomdev.awery.extensions.support.template.CatalogFilter;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.extensions.support.template.CatalogVideo;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base class for all extension providers.
 * Please note that this class can be a ExtensionProviderGroup, which doesn't implement media source methods.
 * @author MrBoomDev
 */
@SuppressWarnings("unused")
public abstract class ExtensionProvider implements Comparable<ExtensionProvider> {
	public static final int FEATURE_READ_MEDIA_COMMENTS = 1;
	public static final int FEATURE_LOGIN = 2;
	public static final int FEATURE_WATCH_MEDIA = 3;
	public static final int FEATURE_READ_MEDIA = 4;
	public static final int FEATURE_WRITE_MEDIA_COMMENTS = 5;
	public static final int FEATURE_TRACK = 6;
	public static final int FEATURE_COMMENTS_SORT = 7;

	@Override
	public int compareTo(@NonNull ExtensionProvider o) {
		if(getName().equals(o.getName())) {
			return getLang().compareToIgnoreCase(o.getLang());
		}

		return getName().compareToIgnoreCase(o.getName());
	}

	public void search(CatalogFilter filter, @NonNull ResponseCallback<List<? extends CatalogMedia>> callback) {
		callback.onFailure(new UnimplementedException("Search not implemented!"));
	}

	/**
	 * @param context The Android context
	 * @param callback Will be ran on success or failure
	 * @author MrBoomDev
	 */
	public void getSettings(Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		callback.onFailure(new UnimplementedException("Settings not implemented!"));
	}

	public void readMediaComments(
			CatalogMedia media,
			CatalogEpisode episode,
			@NonNull ResponseCallback<CatalogComment> callback
	) {
		callback.onFailure(new UnimplementedException("Comments aren't implemented!"));
	}

	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<? extends CatalogEpisode>> callback) {
		callback.onFailure(new UnimplementedException("Episodes not implemented!"));
	}

	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		callback.onFailure(new UnimplementedException("Videos not implemented!"));
	}

	/**
	 * @return An collection of constants representing features supported by the extension
	 * @author MrBoomDev
	 */
	public abstract Collection<Integer> getFeatures();

	/**
	 * @param feature A constant from {@link ExtensionProvider} representing the feature.
	 * @return Whether the extension supports the feature
	 * @author MrBoomDev
	 */
	public boolean hasFeature(int feature) {
		return getFeatures().contains(feature);
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