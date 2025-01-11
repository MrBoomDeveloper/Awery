package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.platform.PlatformResourcesKt.i18n;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.findIndex;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceScreen;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.ext.data.CatalogMedia;
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogVideoFile;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;
import com.mrboomdev.awery.generated.Res;
import com.mrboomdev.awery.generated.String0_commonMainKt;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.adapters.MediaAdapter;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.AnimeSource;
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource;
import eu.kanade.tachiyomi.animesource.model.AnimeFilter;
import eu.kanade.tachiyomi.animesource.model.AnimesPage;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;
import java9.util.stream.Collectors;
import kotlin.NotImplementedError;
import kotlin.UninitializedPropertyAccessException;
import okhttp3.Headers;

public abstract class AniyomiProvider extends YomiProvider {
	private static final String TAG = "AniyomiProvider";
	private static final String FEED_LATEST = "latest";
	private static final String FEED_POPULAR = "popular";
	protected final AnimeSource source;
	private final Set<String> features = new HashSet<>();
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
	public AdultContent getAdultContentMode() {
		if(getExtension().isNsfw()) {
			return AdultContent.ONLY;
		}

		return AdultContent.NONE;
	}

	@Override
	public String getPreviewUrl() {
		if(source instanceof AnimeHttpSource httpSource) {
			return httpSource.getBaseUrl();
		}

		return null;
	}

	@Override
	public AsyncFuture<SettingsList> getFilters() {
		if(source instanceof AnimeCatalogueSource catalogueSource) {
			return AsyncUtils.futureNow(new SettingsList(stream(catalogueSource.getFilterList())
					.map(AniyomiProvider::mapAnimeFilter)
					.filter(Objects::nonNull)
					.toList()));
		} else {
			return AsyncUtils.futureFailNow(new NotImplementedError("Filters aren't supported!"));
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
	public AsyncFuture<List<? extends CatalogVideo>> getVideos(@NonNull SettingsList filters) {
		return thread(() -> {
			var media = App.Companion.getMoshi(MediaAdapter.INSTANCE).adapter(CatalogMedia.class)
					.fromJson(filters.require(ExtensionProvider.FILTER_MEDIA).getStringValue());

			var episodes = AniyomiKotlinBridge.getEpisodesList(source, AniyomiMediaKt.toSAnime(media)).await();

			if(episodes == null || episodes.isEmpty()) {
				throw new ZeroResultsException("Aniyomi: No episodes found", i18n(String0_commonMainKt.getNo_episodes_found(Res.string.INSTANCE)));
			}

			return stream(episodes)
					.map(ep -> new AniyomiEpisode(this, ep))
					.toList();
		});
	}

	@Override
	public AsyncFuture<List<CatalogVideoFile>> getVideoFiles(@NonNull SettingsList filters) {
		return thread(() -> {
			var episode = (CatalogVideo) filters.require(
					ExtensionProvider.FILTER_EPISODE).getSerializable();

			var videos = AniyomiKotlinBridge.getVideosList(
					source, AniyomiEpisode.fromEpisode(episode)).await();

			if(videos == null || videos.isEmpty()) {
				throw new ZeroResultsException("Aniyomi: No videos found", i18n(String0_commonMainKt.getNothing_found(Res.string.INSTANCE)));
			}

			return stream(videos).map(item -> new CatalogVideoFile(
					item.getQuality(),
					item.getVideoUrl(),

					item.getHeaders() == null ? null :
							mapHttpHeaders(item.getHeaders()),

					stream(item.getSubtitleTracks())
							.map(track -> new CatalogSubtitle(track.getLang(), track.getUrl()))
							.toList()
				)).collect(Collectors.toCollection(ArrayList::new));
		});
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
	public Set<String> getFeatures() {
		return features;
	}

	@Contract("null -> fail")
	private void checkSearchResults(AnimesPage page) throws ZeroResultsException {
		if(page == null) {
			throw new NullPointerException("page is null!");
		}

		if(page.getAnimes().isEmpty()) {
			throw new ZeroResultsException("No media was found", i18n(String0_commonMainKt.getNo_media_found(Res.string.INSTANCE)));
		}
	}

	@Override
	public AsyncFuture<CatalogMedia> getMedia(String id) {
		return AniyomiKotlinBridge.getAnimeDetails(source, new SAnimeImpl() {{
			setUrl(id);
		}}).then(anime -> {
			if(anime == null) {
				throw new ZeroResultsException("Anime not found", i18n(String0_commonMainKt.getNo_media_found(Res.string.INSTANCE)));
			}

			// Manually set values if they wasn't been by an extension
			try { anime.getUrl(); } catch(UninitializedPropertyAccessException ex) { anime.setUrl(id); }
			try { anime.getTitle(); } catch(UninitializedPropertyAccessException ex) { anime.setTitle(id); }

			return AniyomiMediaKt.toMedia(anime, this);
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
					this.extensionId = getExtension().getId();
					this.sourceId = getId();
					this.sourceFeed = FEED_LATEST;
					this.displayMode = DisplayMode.LIST_HORIZONTAL;
				}});
			}

			feeds.add(new CatalogFeed() {{
				this.id = getId() + "_feed_popular";
				this.title = "Popular in " + getName();
				this.sourceManager = AniyomiManager.MANAGER_ID;
				this.extensionId = getExtension().getId();
				this.sourceId = getId();
				this.sourceFeed = FEED_POPULAR;
				this.displayMode = DisplayMode.LIST_HORIZONTAL;
			}});

			callback.onSuccess(feeds);
		} else {
			callback.onFailure(new NotImplementedError("AnimeSource doesn't extend the AnimeCatalogueSource!"));
		}
	}

	@Override
	public AsyncFuture<CatalogSearchResults<? extends CatalogMedia>> searchMedia(@NonNull SettingsList filters) {
		if(source instanceof AnimeCatalogueSource catalogueSource) {
			var query = filters.get(FILTER_QUERY);
			var page = filters.get(FILTER_PAGE);
			var feed = filters.get(FILTER_FEED);

			AsyncFuture<AnimesPage> future;

			// filters.size() <= 2 only if query and page filters are being met.
			if(feed != null && feed.getStringValue() != null && filters.size() <= 2) {
				switch(feed.getStringValue()) {
					case FEED_LATEST -> future = AniyomiKotlinBridge.getLatestAnime(
							catalogueSource, requireNonNullElse(page.getIntegerValue(), 0));

					case FEED_POPULAR -> future = AniyomiKotlinBridge.getPopularAnime(
							catalogueSource, requireNonNullElse(page.getIntegerValue(), 0));

					default -> {
						return AsyncUtils.futureFailNow(new IllegalArgumentException("Unknown feed! " + feed));
					}
				}
			} else {
				var animeFilters = catalogueSource.getFilterList();
				applyFilters(animeFilters, filters);

				future = AniyomiKotlinBridge.searchAnime(catalogueSource,
						requireNonNullElse(page.getIntegerValue(), 0), query.getStringValue(), animeFilters);
			}

			return future.then(animePage -> {
				checkSearchResults(animePage);

				return CatalogSearchResults.of(stream(animePage.getAnimes())
						.map(item -> AniyomiMediaKt.toMedia(item, this))
						.toList(), animePage.getHasNextPage());
			});
		} else {
			return AsyncUtils.futureFailNow(new NotImplementedError("AnimeSource doesn't extend the AnimeCatalogueSource!"));
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
		} else {
			throw new UnsupportedOperationException();
		}
	}
}