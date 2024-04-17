package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

import androidx.preference.PreferenceScreen;

import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;

import java.util.Collection;
import java.util.List;

import eu.kanade.tachiyomi.source.ConfigurableSource;
import eu.kanade.tachiyomi.source.MangaSource;

public class TachiyomiProvider extends YomiProvider {
	private final Collection<Integer> FEATURES = List.of(FEATURE_MEDIA_READ, FEATURE_MEDIA_SEARCH);
	private final MangaSource source;

	public TachiyomiProvider(MangaSource source) {
		this.source = source;
	}

	@Override
	public void setupPreferenceScreen(PreferenceScreen screen) {
		if(source instanceof ConfigurableSource configurableSource) {
			configurableSource.setupPreferenceScreen(screen);
		}
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