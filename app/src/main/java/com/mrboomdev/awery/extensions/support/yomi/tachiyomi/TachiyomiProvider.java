package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

import androidx.preference.PreferenceScreen;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;

import java.util.HashSet;
import java.util.Set;

import eu.kanade.tachiyomi.source.ConfigurableSource;
import eu.kanade.tachiyomi.source.MangaSource;
import eu.kanade.tachiyomi.source.online.HttpSource;

public abstract class TachiyomiProvider extends YomiProvider {
	private final Set<String> features = new HashSet<>();
	private final MangaSource source;

	public TachiyomiProvider(__Extension extension, MangaSource source, boolean isFromSource) {
		super(extension);
		this.features.addAll(getManager().getBaseFeatures());
		this.source = source;
	}

	@Override
	public AdultContent getAdultContentMode() {
		if(getExtension().isNsfw()) {
			return AdultContent.ONLY;
		}

		return AdultContent.NONE;
	}

	@Override
	public String getPreviewUrl() {
		if(source instanceof HttpSource httpSource) {
			return httpSource.getBaseUrl();
		}

		return null;
	}

	@Override
	public void setupPreferenceScreen(PreferenceScreen screen) {
		if(source instanceof ConfigurableSource configurableSource) {
			configurableSource.setupPreferenceScreen(screen);
		}
	}

	@Override
	public Set<String> getFeatures() {
		return features;
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