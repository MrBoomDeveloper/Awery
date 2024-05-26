package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.util.NiceUtils.findIn;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceScreen;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;
import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource;
import eu.kanade.tachiyomi.animesource.model.AnimesPage;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;
import java9.util.stream.Collectors;
import kotlin.UninitializedPropertyAccessException;
import okhttp3.Headers;

public class AniyomiProvider extends YomiProvider {
	protected final AnimeCatalogueSource source;
	private final List<Integer> features = new ArrayList<>();
	private final boolean isFromSource;

	public AniyomiProvider(YomiManager manager, Extension extension, AnimeCatalogueSource source) {
		super(manager, extension);

		this.features.addAll(manager.getBaseFeatures());

		if(extension.isNsfw()) {
			this.features.add(FEATURE_NSFW);
		}

		this.source = source;
		this.isFromSource = false;
	}

	public AniyomiProvider(YomiManager manager, Extension extension, AnimeCatalogueSource source, boolean isFromSource) {
		super(manager, extension);

		this.features.addAll(manager.getBaseFeatures());

		this.source = source;
		this.isFromSource = isFromSource;
	}

	public boolean isFromSourceFactory() {
		return isFromSource;
	}

	@Override
	public String getPreviewUrl() {
		if(source instanceof AnimeHttpSource httpSource) {
			return httpSource.getBaseUrl();
		}

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
	public void searchMedia(
			Context context,
			List<CatalogFilter> params,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		if(params == null) {
			throw new NullPointerException("params cannot be null!");
		}

		var query = findIn(filter -> filter.getId().equals("query"), params);
		var page = findIn(filter -> filter.getId().equals("page"), params);

		var filter = source.getFilterList();

		new Thread(() -> AniyomiKotlinBridge.searchAnime(source,
				page != null ? page.getIntegerValue() : 0,
				query.getStringValue(), filter, (animePage, t) -> {
			if(!checkSearchResults(animePage, t, callback)) return;

			callback.onSuccess(CatalogSearchResults.of(stream(animePage.getAnimes())
					.map(item -> new AniyomiMedia(this, item))
					.toList(), animePage.getHasNextPage()));
		})).start();
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
}