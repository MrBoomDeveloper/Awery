package com.mrboomdev.awery.extensions;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.extensions.support.js.JsProvider;
import com.mrboomdev.awery.extensions.data.CatalogCategory;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogFilter;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
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
	/**
	 * Isn't used by the application itself, only for the {@link JsProvider}
	 */
	public static final int FEATURE_LOGIN = 2;
	public static final int FEATURE_MEDIA_WATCH = 3;
	public static final int FEATURE_MEDIA_READ = 4;
	public static final int FEATURE_MEDIA_COMMENTS = 5;
	public static final int FEATURE_TRACK = 6;
	public static final int FEATURE_COMMENTS_SORT = 7;
	public static final int FEATURE_COMMENTS_REPORT = 8;
	public static final int FEATURE_COMMENTS_VOTE = 9;
	public static final int FEATURE_MEDIA_REPORT = 10;

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

	public void readMediaComments(ReadMediaCommentsRequest request, @NonNull ResponseCallback<CatalogComment> callback) {
		callback.onFailure(new UnimplementedException("Comments reading aren't implemented!"));
	}

	public void postMediaComment(CatalogComment parent, CatalogComment comment, @NonNull ResponseCallback<CatalogComment> callback) {
		callback.onFailure(new UnimplementedException("Comments posting aren't implemented!"));
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
	public String getName() {
		return getId();
	}

	public abstract String getId();

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