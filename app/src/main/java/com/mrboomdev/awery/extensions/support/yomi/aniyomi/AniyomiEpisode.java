package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogEpisode;

import eu.kanade.tachiyomi.animesource.model.SEpisode;
import eu.kanade.tachiyomi.animesource.model.SEpisodeImpl;

public class AniyomiEpisode extends CatalogEpisode {
	private final SEpisode episode;

	public AniyomiEpisode(@NonNull SEpisode episode) {
		super(episode.getName(),
				episode.getUrl(),
				null,
				null,
				episode.getDate_upload(),
				episode.getEpisode_number());

		this.episode = episode;
	}

	@NonNull
	protected static SEpisode fromEpisode(@NonNull CatalogEpisode episode) {
		var animeEpisode = new SEpisodeImpl();
		animeEpisode.setUrl(episode.getUrl());
		animeEpisode.setDate_upload(episode.getReleaseDate());
		animeEpisode.setName(episode.getTitle());
		animeEpisode.setEpisode_number(episode.getNumber());
		return animeEpisode;
	}

	protected SEpisode getEpisode() {
		return episode;
	}
}