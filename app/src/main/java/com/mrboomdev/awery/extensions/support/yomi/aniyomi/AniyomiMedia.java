package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.util.Parser;

import java.io.IOException;

import eu.kanade.tachiyomi.animesource.model.SAnime;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import java9.util.stream.Collectors;

public class AniyomiMedia extends CatalogMedia {
	private final SAnime anime;

	public AniyomiMedia(@NonNull AniyomiProvider provider, @NonNull SAnime anime) {
		super(AniyomiManager.TYPE_ID + ";;;"
				+ provider.getId() + ";;;"
				+ anime.getUrl());

		this.setTitle(anime.getTitle());
		this.setPoster(anime.getThumbnail_url());

		this.status = switch(anime.getStatus()) {
			case SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> CatalogMedia.MediaStatus.COMPLETED;
			case SAnime.ONGOING -> CatalogMedia.MediaStatus.ONGOING;
			case SAnime.ON_HIATUS -> CatalogMedia.MediaStatus.PAUSED;
			case SAnime.CANCELLED -> CatalogMedia.MediaStatus.CANCELLED;
			default -> CatalogMedia.MediaStatus.UNKNOWN;
		};

		this.extra = Parser.toString(SAnime.class, anime);
		this.description = anime.getDescription();
		this.anime = anime;

		this.authors.put("author", anime.getAuthor());
		this.authors.put("artist", anime.getArtist());

		if(anime.getGenre() != null) {
			this.genres = stream(anime.getGenre().split(", "))
					.map(String::trim)
					.filter(item -> !item.isBlank())
					.toList();
		}
	}

	protected static SAnime fromMedia(CatalogMedia media) {
		if(media instanceof AniyomiMedia animeMedia) {
			return animeMedia.getAnime();
		}

		// Try to restore the SAnime instance from the serialized extra
		if(media.extra != null && media.getManagerId().equals(AniyomiManager.TYPE_ID)) {
			try {
				return Parser.fromString(SAnime.class, media.extra);
			} catch(IOException ignored) {}
		}

		// Well, this media is not an AniyomiMedia, we can try generating one from the media
		var anime = new SAnimeImpl();
		anime.setTitle(media.getTitle());
		anime.setDescription(media.description);
		anime.setThumbnail_url(media.poster.extraLarge);
		anime.setAuthor(media.authors.get("author"));
		anime.setArtist(media.authors.get("artist"));

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