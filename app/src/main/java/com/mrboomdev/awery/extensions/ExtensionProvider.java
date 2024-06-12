package com.mrboomdev.awery.extensions;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.request.PostMediaCommentRequest;
import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.extensions.data.CatalogTrackingOptions;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.extensions.support.js.JsProvider;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base class for all extension providers.
 * @author MrBoomDev
 */
@SuppressWarnings("unused")
public abstract class ExtensionProvider implements Comparable<ExtensionProvider> {
	public static final int FEATURE_TAGS_SEARCH = 1;
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
	public static final int FEATURE_MEDIA_REPORT = 9;
	public static final int FEATURE_MEDIA_SEARCH = 10;
	public static final int FEATURE_COMMENTS_PER_EPISODE = 11;
	public static final int FEATURE_CHANGELOG = 12;
	public static final int FEATURE_COMMENTS_OPEN_ACCOUNT = 13;
	/**
	 * It's more not a feature. Just a mark that this provider is nsfw
	 */
	public static final int FEATURE_NSFW = 14;
	public static final int FEATURE_FEEDS = 15;
	private final ExtensionsManager manager;
	private final Extension extension;

	public ExtensionProvider(ExtensionsManager manager, Extension extension) {
		this.manager = manager;
		this.extension = extension;
	}

	public ExtensionProvider() {
		this.manager = null;
		this.extension = null;
	}

	public Extension getExtension() {
		return extension;
	}

	public ExtensionsManager getManager() {
		return manager;
	}

	@Override
	public int compareTo(@NonNull ExtensionProvider o) {
		if(getName().equals(o.getName())) {
			return getLang().compareToIgnoreCase(o.getLang());
		}

		return getName().compareToIgnoreCase(o.getName());
	}

	public void getMedia(Context context, String id, @NonNull ResponseCallback<CatalogMedia> callback) {
		callback.onFailure(new UnimplementedException("Media obtain isn't implemented!"));
	}

	public void searchMedia(
			Context context,
			List<CatalogFilter> filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		callback.onFailure(new UnimplementedException("Media searching isn't implemented!"));
	}

	/**
	 * @param context The Android context
	 * @param callback Will be ran on success or failure
	 * @author MrBoomDev
	 */
	public void getSettings(Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		callback.onFailure(new UnimplementedException("Settings aren't implemented!"));
	}

	public void readMediaComments(ReadMediaCommentsRequest request, @NonNull ResponseCallback<CatalogComment> callback) {
		callback.onFailure(new UnimplementedException("Comments reading isn't implemented!"));
	}

	public void postMediaComment(PostMediaCommentRequest request, @NonNull ResponseCallback<CatalogComment> callback) {
		callback.onFailure(new UnimplementedException("Comments posting isn't implemented!"));
	}

	public void voteComment(CatalogComment comment, @NonNull ResponseCallback<CatalogComment> callback) {
		callback.onFailure(new UnimplementedException("Comments voting isn't implemented!"));
	}

	public void editComment(CatalogComment oldComment, CatalogComment newComment, @NonNull ResponseCallback<CatalogComment> callback) {
		callback.onFailure(new UnimplementedException("Comments editing isn't implemented!"));
	}

	public void deleteComment(CatalogComment comment, @NonNull ResponseCallback<Boolean> callback) {
		callback.onFailure(new UnimplementedException("Comments deletion isn't implemented!"));
	}

	public void getChangelog(@NonNull ResponseCallback<String> callback) {
		callback.onFailure(new UnimplementedException("Changelog isn't implemented!"));
	}

	public void trackMedia(
			CatalogMedia media,
			@Nullable CatalogTrackingOptions options,
			@NonNull ResponseCallback<CatalogTrackingOptions> callback
	) {
		callback.onFailure(new UnimplementedException("Media tracking isn't implemented!"));
	}

	public void searchTags(@NonNull ResponseCallback<List<CatalogTag>> callback) {
		callback.onFailure(new UnimplementedException("Tags search isn't implemented!"));
	}

	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<? extends CatalogEpisode>> callback) {
		callback.onFailure(new UnimplementedException("Episodes aren't implemented!"));
	}

	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		callback.onFailure(new UnimplementedException("Videos aren't implemented!"));
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

	public String getPreviewUrl() {
		return null;
	}

	/**
	 * @apiNote The returned value can be an array of format: "en;ru;jp"
	 * @return The language of the extension
	 * @author MrBoomDev
	 */
	public String getLang() {
		return "en";
	}

	public void getFeeds(@NonNull ResponseCallback<List<CatalogFeed>> callback) {
		callback.onFailure(new UnimplementedException("Categories aren't implemented!"));
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