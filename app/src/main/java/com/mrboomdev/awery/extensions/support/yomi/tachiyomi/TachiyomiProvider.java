package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import eu.kanade.tachiyomi.source.MangaSource;

public class TachiyomiProvider extends ExtensionProvider {
	private final MangaSource source;

	public TachiyomiProvider(MangaSource source) {
		this.source = source;
	}

	@Override
	public String getName() {
		return source.getName();
	}
}