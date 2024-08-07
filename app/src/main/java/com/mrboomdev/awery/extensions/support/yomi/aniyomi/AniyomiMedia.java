package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;

import java.util.HashMap;

import eu.kanade.tachiyomi.animesource.model.SAnime;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;
import java9.util.stream.Collectors;

public class AniyomiMedia extends CatalogMedia {
	private final SAnime anime;

	public AniyomiMedia(@NonNull AniyomiProvider provider, @NonNull SAnime anime) {
		super(AniyomiManager.MANAGER_ID, provider.getExtension().getId(), provider.getId(), anime.getUrl());

		this.setTitle(anime.getTitle());
		this.setPoster(anime.getThumbnail_url());
		this.type = MediaType.TV;

		this.status = switch(anime.getStatus()) {
			case SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> CatalogMedia.MediaStatus.COMPLETED;
			case SAnime.ONGOING -> CatalogMedia.MediaStatus.ONGOING;
			case SAnime.ON_HIATUS -> CatalogMedia.MediaStatus.PAUSED;
			case SAnime.CANCELLED -> CatalogMedia.MediaStatus.CANCELLED;
			default -> CatalogMedia.MediaStatus.UNKNOWN;
		};

		if(provider.source instanceof AnimeHttpSource httpSource) {
			this.url = YomiProvider.concatLink(httpSource.getBaseUrl(), anime.getUrl());
		}

		this.extra = anime.getUrl();
		this.description = anime.getDescription();
		this.anime = anime;

		if(anime.getAuthor() != null) {
			if(authors == null) authors = new HashMap<>();
			authors.put("Author", anime.getAuthor());
		}

		if(anime.getArtist() != null) {
			if(authors == null) authors = new HashMap<>();
			authors.put("Artist", anime.getArtist());
		}

		doIfNotNull(anime.getGenre(), genre -> {
			this.genres = stream(genre.split(", "))
					.map(String::trim)
					.filter(item -> !item.isBlank())
					.toList();

			this.tags = stream(genre.split(", "))
					.map(String::trim)
					.filter(item -> !item.isBlank())
					.map(CatalogTag::new)
					.toList();
		});
	}

	protected static SAnime fromMedia(CatalogMedia media) {
		if(media instanceof AniyomiMedia animeMedia) {
			return animeMedia.getAnime();
		}

		var anime = new SAnimeImpl();
		anime.setTitle(requireNonNullElse(media.getTitle(), "No title"));
		anime.setDescription(media.description);
		anime.setThumbnail_url(media.getBestPoster());
		anime.setUrl(requireNonNull(media.extra));

		if(media.authors != null) {
			anime.setAuthor(media.authors.get("Author"));
			anime.setArtist(media.authors.get("Artist"));
		}

		if(media.status != null) {
			anime.setStatus(switch(media.status) {
				case ONGOING -> SAnime.ONGOING;
				case COMPLETED -> SAnime.COMPLETED;
				case PAUSED -> SAnime.ON_HIATUS;
				case CANCELLED -> SAnime.CANCELLED;
				case UNKNOWN, COMING_SOON -> 0;
			});
		}

		anime.setGenre(media.genres == null ? null : stream(media.genres)
				.map(String::trim)
				.filter(item -> !item.isBlank())
				.collect(Collectors.joining(", ")));

		return anime;
	}

	protected SAnime getAnime() {
		return anime;
	}
}