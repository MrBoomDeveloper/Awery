package com.mrboomdev.awery.extensions.data;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.squareup.moshi.Json;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Entity(tableName = "feed")
public class CatalogFeed implements Serializable {
	public static final String TEMPLATE_BOOKMARKS = "BOOKMARKS";
	public static final String TEMPLATE_FEATURED = "FEATURED";
	public static final String TEMPLATE_CONTINUE = "CONTINUE";
	public static final String TEMPLATE_AUTO_GENERATE = "AUTO_GENERATE";
	public static final String TEMPLATING_SOURCE_MANAGER = "INTERNAL";
	public static final String TEMPLATING_SOURCE_ID = "TEMPLATE";
	@Serial
	private static final long serialVersionUID = 1;
	@PrimaryKey
	@NonNull
	public String id;
	public int index;
	public List<SettingsItem> filters;
	public String tab, title;
	@ColumnInfo(name = "hide_if_empty")
	@Json(name = "hide_if_empty", ignore = true)
	public boolean hideIfEmpty;
	@ColumnInfo(name = "source_manager")
	@Json(name = "source_manager")
	public String sourceManager;
	@ColumnInfo(name = "source_id")
	@Json(name = "source_id")
	public String sourceId;
	@ColumnInfo(name = "source_feed")
	@Json(name = "source_feed")
	public String sourceFeed;
	@ColumnInfo(name = "source_extension")
	@Json(name = "source_extension")
	public String extensionId;
	public List<String> features = new ArrayList<>();
	@ColumnInfo(name = "display_mode")
	@Json(name = "display_mode")
	public DisplayMode displayMode;

	public CatalogFeed() {
		id = String.valueOf(System.currentTimeMillis());
	}

	public String getProivderGlobalId() {
		return sourceManager + ";;;" + sourceId + ":" + extensionId;
	}

	public ExtensionProvider getSourceProvider() throws ExtensionNotInstalledException {
		return ExtensionProvider.forGlobalId(sourceManager, extensionId, sourceId);
	}

	/**
	 * This method takes raw feeds with templating support and then returns list with processes feeds.
	 * You can think about it as an preprocessor.
	 * @param feeds Raw feeds
	 * @author MrBoomDev
	 */
	public static List<CatalogFeed> processFeeds(@NonNull List<CatalogFeed> feeds) {
		return stream(feeds).map(CatalogFeed::processFeed).flatMap(NiceUtils::stream).toList();
	}

	/**
	 * A heavy task. Run on a separate thread!
	 */
	@NonNull
	private static List<CatalogFeed> processFeed(@NonNull CatalogFeed feed) {
		if(Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("processFeed() was called on the ui thread!");
		}

		if(!feed.sourceManager.equals(TEMPLATING_SOURCE_MANAGER)) {
			return Collections.singletonList(feed);
		}

		return switch(feed.sourceFeed) {
			// TODO: Finish other templates

			case TEMPLATE_BOOKMARKS -> stream(getDatabase().getListDao().getAll())
					.map(list -> {
						var result = new CatalogFeed();
						result.sourceManager = TEMPLATING_SOURCE_MANAGER;
						result.sourceId = TEMPLATE_BOOKMARKS;
						result.extensionId = TEMPLATE_BOOKMARKS;
						result.sourceFeed = list.getId();
						result.title = list.getName();
						result.hideIfEmpty = true;
						return result;
					})
					.toList();

			case TEMPLATE_AUTO_GENERATE -> stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
					.map(Extension::getProviders)
					.flatMap(NiceUtils::stream)
					.filter(provider -> {
						if(!provider.hasFeatures(ExtensionProvider.FEATURE_FEEDS)) {
							return false;
						}

						var adultMode = AwerySettings.ADULT_MODE.getValue();

						if(adultMode != null) {
							switch(adultMode) {
								case SAFE -> {
									switch(provider.getAdultContentMode()) {
										case ONLY, PARTIAL -> {
											return false;
										}
									}
								}

								case ONLY -> {
									if(provider.getAdultContentMode() == ExtensionProvider.AdultContent.NONE) {
										return false;
									}
								}
							}
						}

						return true;
					})
					.map(provider -> {
						var feeds = new AtomicReference<List<CatalogFeed>>();

						provider.getFeeds(new ExtensionProvider.ResponseCallback<>() {
							@Override
							public void onSuccess(List<CatalogFeed> catalogFeeds) {
								feeds.set(catalogFeeds);
							}

							@Override
							public void onFailure(Throwable e) {
								feeds.set(Collections.emptyList());
							}
						});

						while(feeds.get() == null);
						return feeds.get();
					})
					.flatMap(NiceUtils::stream)
					.toList();

			default -> Collections.emptyList();
		};
	}

	public enum DisplayMode {
		LIST_HORIZONTAL, LIST_VERTICAL, SLIDES, GRID
	}
}