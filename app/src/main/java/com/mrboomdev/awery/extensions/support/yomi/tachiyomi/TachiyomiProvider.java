package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.content.SharedPreferences;

import androidx.preference.PreferenceScreen;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;
import eu.kanade.tachiyomi.source.ConfigurableSource;
import eu.kanade.tachiyomi.source.MangaSource;
import eu.kanade.tachiyomi.source.online.HttpSource;

public class TachiyomiProvider extends YomiProvider {
	private final List<Integer> features = new ArrayList<>();
	private final MangaSource source;

	public TachiyomiProvider(YomiManager manager, Extension extension, MangaSource source) {
		super(manager, extension);

		this.features.addAll(manager.getBaseFeatures());

		if(extension.isNsfw()) {
			this.features.add(FEATURE_NSFW);
		}

		this.source = source;
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
	public Collection<Integer> getFeatures() {
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

	@Override
	protected SharedPreferences getSharedPreferences() {
		return getAnyContext().getSharedPreferences("source_" + source.getId(), 0);
	}
}