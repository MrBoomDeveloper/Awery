package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.extensions.data.CatalogTrackingOptions;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogVideoFile;
import com.mrboomdev.awery.extensions.request.PostMediaCommentRequest;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.extensions.support.js.JsProvider;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Base class for all extension providers.
 * @author MrBoomDev
 */
/* TODO: REPLACE ALL METHOD ARGUMENTS WITH A SINGLE SettingsList */
@SuppressWarnings("unused")
public abstract class ExtensionProvider implements Comparable<ExtensionProvider> {
	public static final String FILTER_VIDEO_CATEGORY = "FILTER_VIDEO_CATEGORY";
	public static final String FILTER_PAGE = "FILTER_PAGE";
	public static final String FILTER_SEASON = "FILTER_SEASON";
	public static final String FILTER_EPISODE = "FILTER_EPISODE";
	public static final String FILTER_FEED = "FILTER_FEED";
	public static final String FILTER_QUERY = "FILTER_QUERY";
	public static final String FILTER_TAGS = "FILTER_TAGS";
	public static final String FILTER_MEDIA = "FILTER_MEDIA";

	public static final String VIDEO_CATEGORY_EPISODE = "VIDEO_CATEGORY_EPISODE";
	public static final String VIDEO_CATEGORY_TRAILER = "VIDEO_CATEGORY_TRAILER";
	public static final String VIDEO_CATEGORY_MUSIC = "VIDEO_CATEGORY_MUSIC";

	/**
	 * Isn't used by the application itself, only for the {@link JsProvider}
	 */
	public static final String FEATURE_LOGIN = "ACCOUNT_LOGIN";
	public static final String FEATURE_TAGS_SEARCH = "SEARCH_TAGS";
	public static final String FEATURE_MEDIA_WATCH = "MEDIA_WATCH";
	public static final String FEATURE_MEDIA_READ = "MEDIA_READ";
	public static final String FEATURE_MEDIA_COMMENTS = "MEDIA_COMMENTS";
	public static final String FEATURE_TRACK = "ACCOUNT_TRACK";
	public static final String FEATURE_COMMENTS_FILTERS = "MEDIA_COMMENTS_FILTERS";
	public static final String FEATURE_COMMENTS_REPORT = "MEDIA_COMMENTS_REPORT";
	public static final String FEATURE_MEDIA_REPORT = "MEDIA_REPORT";
	public static final String FEATURE_MEDIA_SEARCH = "SEARCH_MEDIA";
	public static final String FEATURE_COMMENTS_PER_EPISODE = "MEDIA_COMMENTS_PER_PAGE";
	public static final String FEATURE_CHANGELOG = "CHANGELOG";
	public static final String FEATURE_SEARCH_SUBTITLES = "SEARCH_SUBTITLES";
	public static final String FEATURE_COMMENTS_OPEN_ACCOUNT = "MEDIA_COMMENTS_ACCOUNTS";
	/**
	 * It's more not a feature. Just a mark that this provider is nsfw
	 */
	public static final String FEATURE_NSFW = "NSFW";
	public static final String FEATURE_FEEDS = "FEEDS";
	private final Extension extension;

	public ExtensionProvider(Extension extension) {
		this.extension = extension;
	}

	public ExtensionProvider() {
		this.extension = null;
	}

	public Extension getExtension() {
		return extension;
	}

	public abstract ExtensionsManager getManager();

	public String getGlobalId() {
		return getManager().getId() + ";;;" + getId() + ":" + getExtension().getId();
	}

	public static ExtensionProvider forGlobalId(String managerId, String extensionId, String providerId) throws ExtensionNotInstalledException {
		return forGlobalId(managerId + ";;;" + providerId + ":" + extensionId);
	}

	/**
	 * Format of the globalId:
	 * <p>{@code MANAGER_ID;;;PROVIDER_ID:EXTENSION_ID}</p>
	 * @param globalId May be CatalogMedia's globalId
	 * @return May return an irreverent provider if no EXTENSION_ID was specified
	 */
	public static ExtensionProvider forGlobalId(@NonNull String globalId) throws ExtensionNotInstalledException {
		var split = globalId.split(";;;");
		var split2 = split[1].split(":");

		var managerId = split[0];
		var providerId = split2[0];
		var extensionId = split2[1];

		try {
			return stream(ExtensionsFactory.getManager__Deprecated(managerId)
					.getExtensions(Extension.FLAG_WORKING))
					.map(Extension::getProviders)
					.flatMap(NiceUtils::stream)
					.filter(provider -> {
						// In previous versions extension id wasn't been saved
						if(extensionId != null && !extensionId.isBlank() && !extensionId.equals("null")
								&& !extensionId.equals(provider.getExtension().getId())) return false;

						return providerId.equals(provider.getId());
					}).findFirst().orElseThrow();
		} catch(NoSuchElementException e) {
			throw new ExtensionNotInstalledException("Extension \"" + extensionId + "\" isn't installed!", e);
		}
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
			SettingsList filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		callback.onFailure(new UnimplementedException("Media searching isn't implemented!"));
	}

	public void searchSubtitles(
			Context context,
			SettingsList filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogSubtitle>> callback
	) {
		callback.onFailure(new UnimplementedException("Subtitles search isn't implemented!"));
	}

	public void getFilters(@NonNull ResponseCallback<List<SettingsItem>> callback) {
		callback.onFailure(new UnimplementedException("Filters aren't implemented!"));
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

	public void getVideos(SettingsList filters, @NonNull ResponseCallback<List<? extends CatalogVideo>> callback) {
		callback.onFailure(new UnimplementedException("Episodes aren't implemented!"));
	}

	public void getVideoFiles(SettingsList filters, @NonNull ResponseCallback<List<CatalogVideoFile>> callback) {
		callback.onFailure(new UnimplementedException("Videos aren't implemented!"));
	}

	/**
	 * @return An collection of constants representing features supported by the extension
	 * @author MrBoomDev
	 */
	public abstract Set<String> getFeatures();

	/**
	 * @param features Constants array from {@link com.mrboomdev.awery.sdk.extensions.ExtensionProvider} representing new features
	 * @return Whether the extension supports the feature
	 * @author MrBoomDev
	 */
	public boolean hasFeatures(@NonNull String... features) {
		return getFeatures().containsAll(Arrays.asList(features));
	}

	/**
	 * @return A human-readable name of the extension
	 * @author MrBoomDev
	 */
	public String getName() {
		return getId();
	}

	public abstract String getId();

	public abstract AdultContent getAdultContentMode();

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

	public enum AdultContent {
		ONLY,
		NONE,
		PARTIAL,
		/**
		 * The neutral one.
		 */
		HIDDEN
	}
}