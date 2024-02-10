package com.mrboomdev.awery.catalog.provider;

import java.util.List;

import eu.kanade.tachiyomi.animesource.AnimeSource;
import eu.kanade.tachiyomi.source.MangaSource;

public class TachiyomiExtensionProvider extends ExtensionProvider {
	private final List<AnimeSource> animeSources;
	private final List<MangaSource> mangaSources;

	public TachiyomiExtensionProvider(List<AnimeSource> anime, List<MangaSource> manga) {
		this.animeSources = anime;
		this.mangaSources = manga;
	}
}