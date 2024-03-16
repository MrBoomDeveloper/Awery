package com.mrboomdev.awery.catalog.extensions.support.yomi.aniyomi;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.extensions.ExtensionProvider;
import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogFilter;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogSubtitle;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.model.SAnime;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.model.SEpisodeImpl;
import okhttp3.Headers;

public class AniyomiProvider extends ExtensionProvider {
	public static final String EXTENSION_TYPE = "ANIYOMI_KT";
	private final AnimeCatalogueSource source;

	public AniyomiProvider(AnimeCatalogueSource source) {
		this.source = source;
	}

	@Override
	public void getEpisodes(
			int page,
			@NonNull CatalogMedia media,
			@NonNull ResponseCallback<List<CatalogEpisode>> callback
	) {
		var anime = new SAnimeImpl();
		anime.setTitle(media.title);
		anime.setUrl(media.url);
		anime.setDescription(media.description);
		anime.setThumbnail_url(media.poster.extraLarge);

		new Thread(() -> AniyomiKotlinBridge.getEpisodesList(source, anime, (episodes, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(episodes == null || episodes.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No episodes found"));
				return;
			}

			callback.onSuccess(Objects.requireNonNull(episodes)
					.stream().map(item -> new CatalogEpisode(
							item.getName(),
							item.getUrl(),
							null,
							null,
							item.getDate_upload(),
							item.getEpisode_number()
					)).collect(Collectors.toCollection(ArrayList::new)));
		})).start();
	}

	@Override
	public void getVideos(@NonNull CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		var animeEpisode = new SEpisodeImpl();
		animeEpisode.setUrl(episode.getUrl());
		animeEpisode.setDate_upload(episode.getReleaseDate());
		animeEpisode.setName(episode.getTitle());
		animeEpisode.setEpisode_number(episode.getNumber());

		new Thread(() -> AniyomiKotlinBridge.getVideosList(source, animeEpisode, (videos, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(videos == null || videos.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No videos found"));
				return;
			}

			callback.onSuccess(Objects.requireNonNull(videos)
					.stream().map(item -> {
						var headers = item.getHeaders();

						var subtitles = item.getSubtitleTracks().stream().map(track ->
								new CatalogSubtitle(track.getLang(), track.getUrl())).collect(Collectors.toList());

						return new CatalogVideo(
								item.getQuality(),
								item.getVideoUrl(),
								headers != null ? headers.toString() : "",
								subtitles
						);
					}).collect(Collectors.toCollection(ArrayList::new)));
		})).start();
	}

	@NonNull
	private Bundle getHeaders(@NonNull Headers headers) {
		var bundle = new Bundle();

		for(var header : headers) {
			bundle.putString(header.getFirst(), header.getSecond());
		}

		return bundle;
	}

	@Override
	public void search(CatalogFilter params, @NonNull ResponseCallback<List<CatalogMedia>> callback) {
		var filter = source.getFilterList();

		new Thread(() -> AniyomiKotlinBridge.searchAnime(source, params.getPage(), params.getQuery(), filter, (pag, t) -> {
			if(t != null) {
				callback.onFailure(t);
				return;
			}

			var animes = Objects.requireNonNull(pag).getAnimes();

			if(animes.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Found nothing in the catalog. Try changing your query."));
				return;
			}

			callback.onSuccess(animes.stream().map(item -> {
				var media = new CatalogMedia(EXTENSION_TYPE + ";;;" + getName() + ";;;" + item.getUrl());
				media.setTitle(item.getTitle());
				media.setPoster(item.getThumbnail_url());

				media.status = switch(item.getStatus()) {
					case SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> CatalogMedia.MediaStatus.COMPLETED;
					case SAnime.ONGOING -> CatalogMedia.MediaStatus.ONGOING;
					case SAnime.ON_HIATUS -> CatalogMedia.MediaStatus.PAUSED;
					case SAnime.CANCELLED -> CatalogMedia.MediaStatus.CANCELLED;
					default -> CatalogMedia.MediaStatus.UNKNOWN;
				};

				media.url = item.getUrl();
				media.genres = item.getGenres();
				media.description = item.getDescription();

				return media;
			}).collect(Collectors.toList()));
		})).start();
	}

	@NonNull
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getLang() {
		return source.getLang();
	}

	@Override
	public String getName() {
		return source.getName();
	}
}