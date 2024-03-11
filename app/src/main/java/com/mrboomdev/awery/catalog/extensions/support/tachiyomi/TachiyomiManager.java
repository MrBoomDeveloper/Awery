package com.mrboomdev.awery.catalog.extensions.support.tachiyomi;

import android.util.Log;

import com.mrboomdev.awery.catalog.extensions.Extension;
import com.mrboomdev.awery.catalog.extensions.support.yomi.YomiManager;

import eu.kanade.tachiyomi.source.MangaSource;
import eu.kanade.tachiyomi.source.SourceFactory;

public class TachiyomiManager extends YomiManager {

	@Override
	public String getName() {
		return "Tachiyomi";
	}

	@Override
	public String getId() {
		return "TACHIYOMI_KOTLIN";
	}

	@Override
	public String getMainClassMeta() {
		return "tachiyomi.extension.class";
	}

	@Override
	public String getNsfwMeta() {
		return "tachiyomi.extension.nsfw";
	}

	@Override
	public String getRequiredFeature() {
		return "tachiyomi.extension";
	}

	@Override
	public String getPrefix() {
		return "Tachiyomi: ";
	}

	@Override
	public double getMinVersion() {
		return 1.2;
	}

	@Override
	public double getMaxVersion() {
		return 1.5;
	}

	@Override
	public void fillProvider(Extension extension, Object main) {
		if(main instanceof MangaSource source) {
			extension.addProvider(new TachiyomiProvider(source));
		} else if(main instanceof SourceFactory factory) {
			for(var source : factory.createSources()) {
				fillProvider(extension, source);
			}
		} else {
			Log.e("TachiyomiManager", "Unknown source type: " + main.getClass().getName());
		}
	}
}