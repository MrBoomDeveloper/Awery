package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;

import java.io.Serializable;

import eu.kanade.tachiyomi.animesource.model.SEpisode;
import eu.kanade.tachiyomi.animesource.model.SEpisodeImpl;
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;

public class AniyomiEpisode extends CatalogVideo implements Serializable {
	private final SEpisode episode;

	public AniyomiEpisode(@NonNull AniyomiProvider provider, @NonNull SEpisode episode) {
		super(episode.getName(),
				provider.source instanceof AnimeHttpSource httpSource ? YomiProvider.concatLink(
						httpSource.getBaseUrl(), episode.getUrl()) : episode.getUrl(),
				null,
				null,
				episode.getDate_upload(),
				episode.getEpisode_number());

		this.episode = episode;
	}

	@NonNull
	protected static SEpisode fromEpisode(@NonNull CatalogVideo episode) {
		if(episode instanceof AniyomiEpisode aniyomiEpisode) {
			return aniyomiEpisode.episode;
		}

		var animeEpisode = new SEpisodeImpl();
		animeEpisode.setUrl(episode.getUrl());
		animeEpisode.setDate_upload(episode.getReleaseDate());
		animeEpisode.setName(episode.getTitle());
		animeEpisode.setEpisode_number(episode.getNumber());
		return animeEpisode;
	}
}