package com.mrboomdev.awery.catalog.provider;

import eu.kanade.tachiyomi.source.MangaSource;

public class TachiyomiExtensionProvider extends ExtensionProvider {
	private final MangaSource source;

	public TachiyomiExtensionProvider(MangaSource source) {
		this.source = source;
	}

	@Override
	public String getName() {
		return source.getName();
	}
}