package com.mrboomdev.awery.extensions.data;

import static com.mrboomdev.awery.util.NiceUtils.isTrue;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.squareup.moshi.Json;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "feed")
public class CatalogFeed implements Serializable {
	public static final String TEMPLATE_BOOKMARKS = "BOOKMARKS";
	public static final String TEMPLATE_FEATURED = "FEATURED";
	public static final String TEMPLATE_CONTINUE = "CONTINUE";
	public static final String TEMPLATE_AUTO_GENERATE = "AUTO_GENERATE";
	public static final String TEMPLATING_SOURCE_MANAGER = "INTERNAL";
	public static final String TEMPLATING_SOURCE_ID = "TEMPLATE";

	public static final String FILTER_FIRST_LARGE = "first_large";

	@Serial
	private static final long serialVersionUID = 1;
	@PrimaryKey
	@NonNull
	public String id;
	public int index;
	public SettingsList filters;
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
	public DisplayMode displayMode = DisplayMode.LIST_HORIZONTAL;

	public CatalogFeed() {
		id = String.valueOf(System.currentTimeMillis());
	}

	public CatalogFeed(@NonNull CatalogFeed original) {
		id = original.id;
		index = original.index;
		hideIfEmpty = original.hideIfEmpty;
		sourceManager = original.sourceManager;
		sourceId = original.sourceId;
		sourceFeed = original.sourceFeed;
		extensionId = original.extensionId;
		tab = original.tab;
		title = original.title;
		displayMode = original.displayMode;

		if(original.features != null) {
			features = List.copyOf(original.features);
		}

		if(filters != null) {
			filters = new SettingsList(stream(filters)
					.map(SettingsItem::new)
					.toList());
		}
	}

	public String getProviderGlobalId() {
		return sourceManager + ";;;" + sourceId + ":" + extensionId;
	}

	/**
	 * This method takes raw feeds with templating support and then returns list with processes feeds.
	 * You can think about it as an preprocessor.
	 * @param feeds Raw feeds
	 * @author MrBoomDev
	 */
	public static List<CatalogFeed> processFeeds(@NonNull List<CatalogFeed> feeds) {
		return stream(feeds)
				.map(CatalogFeed::processFeed)
				.flatMap(NiceUtils::stream)
				.toList();
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

			case TEMPLATE_BOOKMARKS -> stream(App.Companion.getDatabase().getListDao().getAll())
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

			case TEMPLATE_AUTO_GENERATE -> {
				var result = new ArrayList<>(stream(ExtensionsFactory.getInstance().await().getExtensions(Extension.FLAG_WORKING))
						.map(Extension::getProviders)
						.flatMap(NiceUtils::stream)
						.filter(provider -> {
							if(!provider.hasFeatures(ExtensionProvider.FEATURE_FEEDS)) {
								return false;
							}
							
							switch(AwerySettings.INSTANCE.getADULT_MODE().getValue()) {
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
							
							return true;
						})
						.map(provider -> AsyncUtils.<List<CatalogFeed>>awaitResult(breaker -> provider.getFeeds(new ExtensionProvider.ResponseCallback<>() {
							@Override
							public void onSuccess(List<CatalogFeed> catalogFeeds) {
								breaker.run(catalogFeeds);
							}

							@Override
							public void onFailure(Throwable e) {
								breaker.run(Collections.emptyList());
							}
						})))
						.flatMap(NiceUtils::stream)
						.toList());

				Collections.shuffle(result);

				if(feed.filters != null) {
					var firstLarge = feed.filters.get(FILTER_FIRST_LARGE);

					if(isTrue(firstLarge.getBooleanValue()) && !result.isEmpty()) {
						result.get(0).displayMode = DisplayMode.SLIDES;
					}
				}

				yield result;
			}

			default -> Collections.emptyList();
		};
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if(obj instanceof CatalogFeed feed) {
			return Objects.equals(feed.sourceFeed, sourceFeed) &&
					Objects.equals(feed.filters, filters) &&
					Objects.equals(feed.displayMode, displayMode) &&
					Objects.equals(feed.getProviderGlobalId(), getProviderGlobalId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		var hashCode = getProviderGlobalId().hashCode();

		if(displayMode != null) {
			hashCode += displayMode.hashCode();
		}

		if(filters != null) {
			hashCode += filters.hashCode();
		}

		if(sourceFeed != null) {
			hashCode += sourceFeed.hashCode();
		}

		if(displayMode != null) {
			hashCode += displayMode.hashCode();
		}

		return hashCode;
	}

	public enum DisplayMode {
		LIST_HORIZONTAL, LIST_VERTICAL, SLIDES, GRID
	}
}