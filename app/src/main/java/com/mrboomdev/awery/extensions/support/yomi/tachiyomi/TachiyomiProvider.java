package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.Collection;
import java.util.List;

import eu.kanade.tachiyomi.source.MangaSource;

public class TachiyomiProvider extends ExtensionProvider {
	private final Collection<Integer> FEATURES = List.of(FEATURE_READ_MEDIA);
	private final MangaSource source;

	public TachiyomiProvider(MangaSource source) {
		this.source = source;
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getName() {
		return source.getName();
	}

	@Override
	public String getId() {
		return String.valueOf(source.getId());
	}
}