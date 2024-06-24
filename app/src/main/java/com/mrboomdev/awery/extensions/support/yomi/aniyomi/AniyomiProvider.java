package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.findIndex;
import static com.mrboomdev.awery.util.NiceUtils.findMap;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public abstract class AniyomiProvider extends YomiProvider {
	private static final String TAG = "AniyomiProvider";
	private static final String FEED_LATEST = "latest";
	private static final String FEED_POPULAR = "popular";
	protected final AnimeSource source;
	private final List<Integer> features = new ArrayList<>();
	private final boolean isFromSource;

	public AniyomiProvider(Extension extension, AnimeSource source) {
		this(extension, source, false);
	}

	public AniyomiProvider(Extension extension, AnimeSource source, boolean isFromSource) {
		super(extension);
		this.source = source;
		this.isFromSource = isFromSource;

		this.features.addAll(getManager().getBaseFeatures());

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
		if(filter instanceof AnimeFilter.CheckBox checkboxFilter) {
			return new SettingsItem.Builder(SettingsItemType.BOOLEAN)
					.setTitle(checkboxFilter.getName())
					.setKey(checkboxFilter.getName())
					.setValue(checkboxFilter.getState())
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.TriState triState) {
			var state = returnWith(triState, it -> {
				if(triState.isIncluded()) return Selection.State.SELECTED;
				if(triState.isExcluded()) return Selection.State.EXCLUDED;
				return Selection.State.UNSELECTED;
			});

			return new SettingsItem.Builder(SettingsItemType.EXCLUDABLE)
					.setTitle(triState.getName())
					.setKey(triState.getName())
					.setValue(state)
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.Select<?> selectFilter) {
			var items = stream(selectFilter.getValues())
					.map(AniyomiProvider::mapAnimeFilter)
					.toList();

			return new SettingsItem.Builder(SettingsItemType.SELECT)
					.setTitle(selectFilter.getName())
					.setKey(selectFilter.getName())
					.setDescription("${VALUE}")
					.setValue(items.get(selectFilter.getState()).getKey())
					.setItems(items)
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.Sort sort) {
			String value = null;

			if(sort.getState() != null) {
				var index = sort.getState().getIndex();

				if(index < sort.getValues().length) {
					value = sort.getValues()[index];
				}
			}

			return new SettingsItem.Builder(SettingsItemType.SELECT)
					.setTitle(sort.getName())
					.setKey(sort.getName())
					.setDescription("${VALUE}")
					.setValue(value)
					.setItems(stream(sort.getValues()).map(sortMode -> new SettingsItem.Builder()
							.setTitle(sortMode)
							.setKey(sortMode)
							.buildCustom()).toList())
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.Text textFilter) {
			return new SettingsItem.Builder(SettingsItemType.STRING)
					.setTitle(textFilter.getName())
					.setKey(textFilter.getName())
					.setDescription("${VALUE}")
					.setValue(textFilter.getState())
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.Group<?> group) {
			return new SettingsItem.Builder(SettingsItemType.SCREEN)
					.setTitle(group.getName())
					.setKey(group.getName())
					.setItems(stream(group.getState()).map(AniyomiProvider::mapAnimeFilter).toList())
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.Header header) {
			return new SettingsItem.Builder(SettingsItemType.CATEGORY)
					.setTitle(header.getName())
					.buildCustom();
		}

		if(filter instanceof AnimeFilter.Separator) {
			return new SettingsItem(SettingsItemType.DIVIDER);
		}

		if(filter instanceof String string) {
			return new SettingsItem.Builder(SettingsItemType.ACTION)
					.setKey(string)
					.setTitle(string)
					.buildCustom();
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

			callback.onSuccess(stream(videos).map(item -> new CatalogVideo(
					item.getQuality(),
					item.getVideoUrl(),

					item.getHeaders() == null ? null :
							mapHttpHeaders(item.getHeaders()),

					stream(item.getSubtitleTracks()).map(track ->
							new CatalogSubtitle(
									track.getLang(),
									track.getUrl()
							)).toList()
			)).collect(Collectors.toCollection(ArrayList::new)));
		})).start();
	}

	@NonNull
	private static Map<String, String> mapHttpHeaders(@NonNull Headers headers) {
		var result = new HashMap<String, String>(headers.size());

		for(var name : headers.names()) {
			result.put(name, headers.get(name));
		}

		return result;
	}

	@Override
	public Collection<Integer> getFeatures() {
		return features;
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
			@NonNull List<SettingsItem> filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		if(source instanceof AnimeCatalogueSource catalogueSource) {
			var query = findMap(filters, filter -> Objects.equals(filter.getKey(), FILTER_QUERY) ? filter.getStringValue() : null);
			var page = findMap(filters, filter -> Objects.equals(filter.getKey(), FILTER_PAGE) ? filter.getIntegerValue() : null);
			var feed = findMap(filters, filter -> Objects.equals(filter.getKey(), FILTER_FEED) ? filter.getStringValue() : null);

			AniyomiKotlinBridge.ResponseCallback<AnimesPage> searchCallback = (animePage, t) -> {
				if(!checkSearchResults(animePage, t, callback)) return;

				callback.onSuccess(CatalogSearchResults.of(stream(animePage.getAnimes())
						.map(item -> new AniyomiMedia(this, item))
						.toList(), animePage.getHasNextPage()));
			};

			// filters.size() <= 2 only if query and page filters are being met.
			if(feed != null && filters.size() <= 2) {
				switch(feed) {
					case FEED_LATEST -> new Thread(() -> AniyomiKotlinBridge.getLatestAnime(
							catalogueSource, requireNonNullElse(page, 0), searchCallback)).start();

					case FEED_POPULAR -> new Thread(() -> AniyomiKotlinBridge.getPopularAnime(
							catalogueSource, requireNonNullElse(page, 0), searchCallback)).start();

					default -> callback.onFailure(new IllegalArgumentException("Unknown feed! " + feed));
				}

				return;
			}

			var animeFilters = catalogueSource.getFilterList();
			applyFilters(animeFilters, filters);

			new Thread(() -> AniyomiKotlinBridge.searchAnime(catalogueSource,
					requireNonNullElse(page, 0), query,
					animeFilters, searchCallback)).start();
		} else {
			callback.onFailure(new UnimplementedException("AnimeSource doesn't extend the AnimeCatalogueSource!"));
		}
	}

	@Contract(pure = true)
	private static void applyFilters(@NonNull List<AnimeFilter<?>> animeFilters, List<? extends SettingsItem> appliedFilters) {
		for(var animeFilter : animeFilters) {
			var found = find(appliedFilters, filter -> Objects.equals(filter.getKey(), animeFilter.getName()));
			if(found == null) continue;

			if(animeFilter instanceof AnimeFilter.Text textFilter) {
				textFilter.setState(found.getStringValue());
			} else if(animeFilter instanceof AnimeFilter.CheckBox booleanFilter) {
				booleanFilter.setState(found.getBooleanValue());
			} else if(animeFilter instanceof AnimeFilter.Select<?> selectFilter) {
				var selected = find(found.getItems(), item -> Objects.equals(item.getKey(), found.getStringValue()));
				if(selected == null) continue;

				selectFilter.setState(found.getItems().indexOf(selected));
			} else if(animeFilter instanceof AnimeFilter.Sort sortFilter) {
				var selected = find(sortFilter.getValues(), value -> value.equals(found.getStringValue()));
				var index = findIndex(sortFilter.getValues(), value -> value.equals(found.getStringValue()));
				if(selected == null || index == null) continue;

				sortFilter.setState(new AnimeFilter.Sort.Selection(index, false));
			} else if(animeFilter instanceof AnimeFilter.TriState triStateFilter) {
				var value = found.getExcludableValue();
				if(value == null) continue;

				triStateFilter.setState(switch(value) {
					case SELECTED -> AnimeFilter.TriState.STATE_INCLUDE;
					case UNSELECTED -> AnimeFilter.TriState.STATE_IGNORE;
					case EXCLUDED -> AnimeFilter.TriState.STATE_EXCLUDE;
				});
			} else if(animeFilter instanceof AnimeFilter.Group<?> group) {
				try {
					@SuppressWarnings("unchecked") var animeFiltersGroup = (List<AnimeFilter<?>>) group.getState();
					applyFilters(animeFiltersGroup, found.getItems());
				} catch(ClassCastException e) {
					toast("Unknown type of the filter group.");
					Log.e(TAG, "Unknown type of the filter group.", e);
				}
			}
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