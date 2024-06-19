package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceScreen;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.AnimeSource;
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource;
import eu.kanade.tachiyomi.animesource.model.AnimeFilter;
import eu.kanade.tachiyomi.animesource.model.AnimesPage;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;
import java9.util.stream.Collectors;
import kotlin.UninitializedPropertyAccessException;
import okhttp3.Headers;

public class AniyomiProvider extends YomiProvider {
	private static final String FEED_LATEST = "latest";
	private static final String FEED_POPULAR = "popular";
	protected final AnimeSource source;
	private final List<Integer> features = new ArrayList<>();
	private final boolean isFromSource;

	public AniyomiProvider(YomiManager manager, Extension extension, AnimeSource source) {
		this(manager, extension, source, false);
	}

	public AniyomiProvider(YomiManager manager, Extension extension, AnimeSource source, boolean isFromSource) {
		super(manager, extension);
		this.source = source;
		this.isFromSource = isFromSource;

		this.features.addAll(manager.getBaseFeatures());

		if(extension.isNsfw()) {
			this.features.add(FEATURE_NSFW);
		}

		if(source instanceof AnimeCatalogueSource) {
			this.features.add(FEATURE_FEEDS);
		}
	}

	@Override
	public String getPreviewUrl() {
		if(source instanceof AnimeHttpSource httpSource) {
			return httpSource.getBaseUrl();
		}

		return null;
	}

	@Override
	public void getFilters(@NonNull ResponseCallback<List<SettingsItem>> callback) {
		if(source instanceof AnimeCatalogueSource catalogueSource) {
			callback.onSuccess(stream(catalogueSource.getFilterList())
					.map(AniyomiProvider::mapAnimeFilter)
					.filter(Objects::nonNull)
					.toList());
		} else {
			callback.onFailure(new UnimplementedException("Filters aren't supported!"));
		}
	}

	@Nullable
	private static SettingsItem mapAnimeFilter(Object filter) {
		if(filter instanceof AnimeFilter.CheckBox selectFilter) {
			return new SettingsItem.Builder(SettingsItemType.BOOLEAN)
					.setTitle(selectFilter.getName())
					.setKey(selectFilter.getName())
					.setValue(selectFilter.getState())
					.build();
		}

		if(filter instanceof AnimeFilter.Select<?> select) {
			var items = stream(select.getValues())
					.map(AniyomiProvider::mapAnimeFilter)
					.toList();

			return new SettingsItem.Builder(SettingsItemType.SELECT)
					.setTitle(select.getName())
					.setKey(select.getName())
					.setValue(items.get(select.getState()).getKey())
					.setItems(items)
					.build();
		}

		if(filter instanceof AnimeFilter.Text textFilter) {
			return new SettingsItem.Builder(SettingsItemType.STRING)
					.setTitle(textFilter.getName())
					.setKey(textFilter.getName())
					.setValue(textFilter.getState())
					.build();
		}

		if(filter instanceof AnimeFilter.Separator) {
			return new SettingsItem(SettingsItemType.DIVIDER);
		}

		if(filter instanceof AnimeFilter.Sort sort) {
			return new SettingsItem.Builder(SettingsItemType.SELECT)
					.setTitle(sort.getName())
					.setKey(sort.getName())
					.setItems(stream(sort.getValues()).map(state -> new SettingsItem.Builder()
							.setTitle(state)
							.setKey(state)
							.build()).toList())
					.build();
		}

		if(filter instanceof AnimeFilter.TriState triState) {
			var state = Selection.State.UNSELECTED;
			if(triState.isIgnored()) state = Selection.State.EXCLUDED;
			if(triState.isIncluded()) state = Selection.State.SELECTED;

			return new SettingsItem.Builder(SettingsItemType.EXCLUDABLE)
					.setTitle(triState.getName())
					.setKey(triState.getName())
					.setValue(state)
					.build();
		}

		if(filter instanceof AnimeFilter.Group<?> group) {
			return new SettingsItem.Builder(SettingsItemType.SCREEN)
					.setTitle(group.getName())
					.setKey(group.getName())
					.setItems(stream(group.getState()).map(AniyomiProvider::mapAnimeFilter).toList())
					.build();
		}

		if(filter instanceof AnimeFilter.Header header) {
			return new SettingsItem.Builder(SettingsItemType.CATEGORY)
					.setTitle(header.getName())
					.build();
		}

		if(filter instanceof String string) {
			return new SettingsItem.Builder(SettingsItemType.ACTION)
					.setKey(string)
					.setTitle(string)
					.build();
		}

		toast("Found an unknown filter! " + filter.getClass().getName(), 1);
		return null;
	}

	@Override
	public void getEpisodes(
			int page,
			@NonNull CatalogMedia media,
			@NonNull ResponseCallback<List<? extends CatalogEpisode>> callback
	) {
		new Thread(() -> AniyomiKotlinBridge.getEpisodesList(source, AniyomiMedia.fromMedia(media), (episodes, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(episodes == null || episodes.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No episodes found", R.string.no_episodes_found));
				return;
			}

			callback.onSuccess(stream(episodes)
					.map(AniyomiEpisode::new).toList());
		})).start();
	}

	@Override
	public void getVideos(@NonNull CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		new Thread(() -> AniyomiKotlinBridge.getVideosList(source, AniyomiEpisode.fromEpisode(episode), (videos, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(videos == null || videos.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No videos found", R.string.nothing_found));
				return;
			}

			callback.onSuccess(stream(videos).map(item -> {
						var headers = item.getHeaders();

						var subtitles = stream(item.getSubtitleTracks()).map(track ->
								new CatalogSubtitle(track.getLang(), track.getUrl())).toList();

						return new CatalogVideo(
								item.getQuality(),
								item.getVideoUrl(),
								headers != null ? headers.toString() : "",
								subtitles
						);
					}).collect(Collectors.toCollection(ArrayList::new)));
		})).start();
	}

	@Override
	public Collection<Integer> getFeatures() {
		return features;
	}

	@NonNull
	private Bundle getHeaders(@NonNull Headers headers) {
		var bundle = new Bundle();

		for(var header : headers) {
			bundle.putString(header.getFirst(), header.getSecond());
		}

		return bundle;
	}

	@Contract("null, _, _ -> false")
	private boolean checkSearchResults(
			AnimesPage page,
			Throwable t,
			ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		if(t != null) {
			callback.onFailure(t);
			return false;
		}

		if(page == null) {
			callback.onFailure(new NullPointerException("page is null!"));
			return false;
		}

		if(page.getAnimes().isEmpty()) {
			callback.onFailure(new ZeroResultsException("No media was found", R.string.no_media_found));
			return false;
		}

		return true;
	}

	@Override
	public void getMedia(Context context, String id, @NonNull ResponseCallback<CatalogMedia> callback) {
		AniyomiKotlinBridge.getAnimeDetails(source, new SAnimeImpl() {{
			setUrl(id);
		}}, (anime, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(anime == null) {
				callback.onFailure(new ZeroResultsException("Anime not found", R.string.no_media_found));
				return;
			}

			try { anime.getUrl(); } catch(UninitializedPropertyAccessException ex) { anime.setUrl(id); }
			try { anime.getTitle(); } catch(UninitializedPropertyAccessException ex) { anime.setTitle(id); }

			callback.onSuccess(new AniyomiMedia(this, anime));
		});
	}

	@Override
	public void getFeeds(@NonNull ResponseCallback<List<CatalogFeed>> callback) {
		if(source instanceof AnimeCatalogueSource catalogueSource) {
			var feeds = new ArrayList<CatalogFeed>();

			if(catalogueSource.getSupportsLatest()) {
				feeds.add(new CatalogFeed() {{
					this.id = getId() + "_feed_latest";
					this.title = "Latest in " + getName();
					this.sourceManager = AniyomiManager.MANAGER_ID;
					this.sourceId = getId();
					this.sourceFeed = FEED_LATEST;
					this.displayMode = DisplayMode.LIST_HORIZONTAL;
				}});
			}

			feeds.add(new CatalogFeed() {{
				this.id = getId() + "_feed_popular";
				this.title = "Popular in " + getName();
				this.sourceManager = AniyomiManager.MANAGER_ID;
				this.sourceId = getId();
				this.sourceFeed = FEED_POPULAR;
				this.displayMode = DisplayMode.LIST_HORIZONTAL;
			}});

			callback.onSuccess(feeds);
		} else {
			callback.onFailure(new UnimplementedException("AnimeSource doesn't extend the AnimeCatalogueSource!"));
		}
	}

	@Override
	public void searchMedia(
			Context context,
			List<SettingsItem> filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		if(source instanceof AnimeCatalogueSource catalogueSource) {
			if(filters == null) {
				throw new NullPointerException("params cannot be null!");
			}

			var query = find(filters, filter -> filter.getKey().equals(FILTER_QUERY));
			var page = find(filters, filter -> filter.getKey().equals(FILTER_PAGE));
			var feed = find(filters, filter -> filter.getKey().equals(FILTER_FEED));

			AniyomiKotlinBridge.ResponseCallback<AnimesPage> searchCallback = (animePage, t) -> {
				if(!checkSearchResults(animePage, t, callback)) return;

				callback.onSuccess(CatalogSearchResults.of(stream(animePage.getAnimes())
						.map(item -> new AniyomiMedia(this, item))
						.toList(), animePage.getHasNextPage()));
			};

			if(feed != null) {
				switch(feed.getStringValue()) {
					case FEED_LATEST -> new Thread(() -> AniyomiKotlinBridge.getLatestAnime(
							catalogueSource, page != null ? page.getIntegerValue() : 0, searchCallback)).start();

					case FEED_POPULAR -> new Thread(() -> AniyomiKotlinBridge.getPopularAnime(
							catalogueSource, page != null ? page.getIntegerValue() : 0, searchCallback)).start();

					default -> callback.onFailure(new IllegalArgumentException("Unknown feed! " + feed));
				}

				return;
			}

			var filter = catalogueSource.getFilterList();

			new Thread(() -> AniyomiKotlinBridge.searchAnime(catalogueSource,
					page != null ? page.getIntegerValue() : 0,
					query.getStringValue(), filter, searchCallback)).start();
		} else {
			callback.onFailure(new UnimplementedException("AnimeSource doesn't extend the AnimeCatalogueSource!"));
		}
	}

	@NonNull
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getId() {
		return String.valueOf(source.getId());
	}

	@Override
	public String getLang() {
		return source.getLang();
	}

	@Override
	public String getName() {
		if(isFromSource) {
			return source.getName() + " [" + source.getLang() + "]";
		}

		return source.getName();
	}

	@Override
	public void setupPreferenceScreen(PreferenceScreen screen) {
		if(source instanceof ConfigurableAnimeSource configurableAnimeSource) {
			configurableAnimeSource.setupPreferenceScreen(screen);
		}
	}

	@Override
	protected SharedPreferences getSharedPreferences() {
		return getAnyContext().getSharedPreferences("source_" + source.getId(), 0);
	}
}