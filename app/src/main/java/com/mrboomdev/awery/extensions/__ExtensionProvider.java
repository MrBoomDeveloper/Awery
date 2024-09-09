package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.util.NiceUtils.asRuntimeException;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.data.settings.base.SettingsItem;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogVideoFile;
import com.mrboomdev.awery.extensions.request.PostMediaCommentRequest;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.ExtensionComponentMissingException;
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
@Deprecated(forRemoval = true)
@SuppressWarnings("unused")
public abstract class __ExtensionProvider implements Comparable<__ExtensionProvider> {
	private final __Extension extension;

	public __ExtensionProvider(__Extension extension) {
		this.extension = extension;
	}

	public __ExtensionProvider() {
		this.extension = null;
	}

	public __Extension getExtension() {
		return extension;
	}

	public abstract ExtensionsManager getManager();

	public String getGlobalId() {
		return getManager().getId() + ";;;" + getId() + ":" + getExtension().getId();
	}

	/**
	 * Format of the globalId:
	 * <p>{@code MANAGER_ID;;;PROVIDER_ID:EXTENSION_ID}</p>
	 * @param globalId May be CatalogMedia's globalId
	 * @return May return an irreverent provider if no EXTENSION_ID was specified
	 */
	@NonNull
	public static __ExtensionProvider forGlobalId(
			@NonNull String globalId
	) throws ExtensionNotInstalledException, ExtensionComponentMissingException {
		var split = globalId.split(";;;");
		var split2 = split[1].split(":");

		var managerId = split[0];
		var providerId = split2[0];
		var extensionId = split2[1];

		try {
			var manager = ExtensionsFactory.getInstance().await().getManager(managerId);
			var extension = manager.getExtension(extensionId);
			return extension.getProvider(providerId);
		} catch(NoSuchElementException e) {
			throw new ExtensionNotInstalledException(globalId, e);
		} catch(ExtensionComponentMissingException e) {
			throw e;
		} catch(Throwable t) {
			throw asRuntimeException(t);
		}
	}

	@Override
	public int compareTo(@NonNull __ExtensionProvider o) {
		if(getName().equals(o.getName())) {
			return getLang().compareToIgnoreCase(o.getLang());
		}

		return getName().compareToIgnoreCase(o.getName());
	}

	public AsyncFuture<CatalogMedia> getMedia(String id) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Media obtain isn't implemented!"));
	}

	public AsyncFuture<CatalogSearchResults<? extends CatalogMedia>> searchMedia(SettingsList filters) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Media searching isn't implemented!"));
	}

	public AsyncFuture<SettingsList> getMediaSearchFilters() {
		return AsyncUtils.futureFailNow(new UnimplementedException("Filters aren't implemented!"));
	}

	public AsyncFuture<SettingsList> getSubtitlesSearchFilters() {
		return AsyncUtils.futureFailNow(new UnimplementedException("Filters aren't implemented!"));
	}

	public AsyncFuture<SettingsList> getCommentFilters() {
		return AsyncUtils.futureFailNow(new UnimplementedException("Filters aren't implemented!"));
	}

	public AsyncFuture<SettingsList> getTrackingFilters() {
		return AsyncUtils.futureFailNow(new UnimplementedException("Filters aren't implemented!"));
	}

	/**
	 * @param context The Android context
	 * @author MrBoomDev
	 */
	public AsyncFuture<SettingsItem> getSettings(Context context) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Settings aren't implemented!"));
	}

	public AsyncFuture<CatalogComment> readMediaComments(SettingsList request) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Comments reading isn't implemented!"));
	}

	public AsyncFuture<CatalogComment> postMediaComment(PostMediaCommentRequest request) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Comments posting isn't implemented!"));
	}

	public AsyncFuture<CatalogComment> voteComment(CatalogComment comment) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Comments voting isn't implemented!"));
	}

	public AsyncFuture<CatalogComment> editComment(CatalogComment oldComment, CatalogComment newComment) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Comments editing isn't implemented!"));
	}

	public AsyncFuture<Boolean> deleteComment(CatalogComment comment) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Comments deletion isn't implemented!"));
	}

	public AsyncFuture<String> getChangelog() {
		return AsyncUtils.futureFailNow(new UnimplementedException("Changelog isn't implemented!"));
	}

	public AsyncFuture<Boolean> trackMedia(SettingsList settings) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Media tracking isn't implemented!"));
	}

	public AsyncFuture<List<? extends CatalogVideo>> getVideos(SettingsList filters) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Episodes aren't implemented!"));
	}

	public AsyncFuture<List<CatalogVideoFile>> getVideoFiles(SettingsList filters) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Videos aren't implemented!"));
	}

	public AsyncFuture<CatalogSearchResults<CatalogSubtitle>> searchSubtitles(SettingsList filters) {
		return AsyncUtils.futureFailNow(new UnimplementedException("Subtitles search isn't implemented!"));
	}

	/**
	 * @return An collection of constants representing features supported by the extension
	 * @author MrBoomDev
	 */
	public abstract Set<String> getFeatures();

	/**
	 * @param features Awery array from {@link com.mrboomdev.awery.sdk.extensions.ExtensionProvider} representing new features
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

	public AsyncFuture<List<CatalogFeed>> getFeeds() {
		return AsyncUtils.futureFailNow(new UnimplementedException("Categories aren't implemented!"));
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
		HIDDEN;

		public boolean hasNsfw() {
			return this == ONLY || this == PARTIAL;
		}
	}
}