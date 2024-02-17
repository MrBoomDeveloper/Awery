package com.mrboomdev.awery.catalog.provider;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.util.CoroutineUtil;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList;
import eu.kanade.tachiyomi.animesource.model.SAnime;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.model.SEpisodeImpl;
import eu.kanade.tachiyomi.network.HttpException;

public class AniyomiExtensionProvider extends ExtensionProvider {
	private final AnimeCatalogueSource source;

	public AniyomiExtensionProvider(AnimeCatalogueSource source) {
		this.source = source;
	}

	@Override
	public void getEpisodes(
			int page,
			@NonNull CatalogMedia media,
			@NonNull ResponseCallback<Collection<CatalogEpisode>> callback
	) {
		var filter = new AnimeFilterList();
		var observable = source.fetchSearchAnime(page, media.titles.get(1), filter);

		new Thread(() -> CoroutineUtil.getObservableValue(observable, (pag, t) -> {
			if(t != null) {
				t.printStackTrace();

				if(t instanceof SocketException || t instanceof HttpException) {
					callback.onFailure(ExtensionProvider.CONNECTION_FAILED);
					return;
				}

				callback.onFailure(t);
				return;
			}

			var animes = Objects.requireNonNull(pag).getAnimes();

			if(animes.isEmpty()) {
				callback.onFailure(ExtensionProvider.ZERO_RESULTS);
				return;
			}

			CoroutineUtil.getObservableValue(source.fetchEpisodeList(animes.get(0)), ((episodes, _t) -> {
				if(_t != null) {
					_t.printStackTrace();
					callback.onFailure(_t);
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
			}));
		})).start();
	}

	@Override
	public void getVideos(@NonNull CatalogEpisode episode, @NonNull ResponseCallback<Collection<CatalogVideo>> callback) {
		var animeEpisode = new SEpisodeImpl();
		animeEpisode.setUrl(episode.getUrl());
		animeEpisode.setDate_upload(episode.getReleaseDate());
		animeEpisode.setName(episode.getTitle());
		animeEpisode.setEpisode_number(episode.getNumber());

		new Thread(() -> CoroutineUtil.getObservableValue(source.fetchVideoList(animeEpisode), ((videos, _t) -> {
			if(_t != null) {
				_t.printStackTrace();
				callback.onFailure(_t);
				return;
			}

			System.out.println(videos.get(0).getVideoUrl());
			System.out.println(videos.get(0).getUrl());

			callback.onSuccess(Objects.requireNonNull(videos)
					.stream().map(item -> new CatalogVideo(
							item.getVideoUrl()
					)).collect(Collectors.toCollection(ArrayList::new)));
		}))).start();
	}

	@Override
	public void search(SearchParams params, @NonNull ResponseCallback<Collection<CatalogMedia>> callback) {
		var filter = new AnimeFilterList();

		new Thread(() -> CoroutineUtil.getObservableValue(source.fetchSearchAnime(
				params.page(),
				params.query(),
				filter
		), (pag, t) -> {
			if(t != null) {
				t.printStackTrace();

				if(t instanceof SocketException || t instanceof HttpException) {
					callback.onFailure(ExtensionProvider.CONNECTION_FAILED);
					return;
				}

				callback.onFailure(t);
				return;
			}

			var animes = Objects.requireNonNull(pag).getAnimes();

			if(animes.isEmpty()) {
				callback.onFailure(ExtensionProvider.ZERO_RESULTS);
				return;
			}

			callback.onSuccess(animes.stream().map(item -> {
				var media = new CatalogMedia();
				media.title = item.getTitle();
				media.url = item.getUrl();
				media.genres = item.getGenres();
				media.description = item.getDescription();
				media.setPoster(item.getThumbnail_url());
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