package com.mrboomdev.awery.catalog.extensions.support.aniyomi;

import android.util.Log;

import com.mrboomdev.awery.catalog.extensions.Extension;
import com.mrboomdev.awery.catalog.extensions.support.yomi.YomiManager;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory;

public class AniyomiManager extends YomiManager {

	@Override
	public String getName() {
		return "Aniyomi";
	}

	@Override
	public String getId() {
		return "ANIYOMI_KOTLIN";
	}

	@Override
	public String getMainClassMeta() {
		return "tachiyomi.animeextension.class";
	}

	@Override
	public String getNsfwMeta() {
		return "tachiyomi.animeextension.nsfw";
	}

	@Override
	public String getRequiredFeature() {
		return "tachiyomi.animeextension";
	}

	@Override
	public String getPrefix() {
		return "Aniyomi: ";
	}

	@Override
	public double getMinVersion() {
		return 12;
	}

	@Override
	public double getMaxVersion() {
		return 15;
	}

	@Override
	public void fillProvider(Extension extension, Object main) {
		if(main instanceof AnimeCatalogueSource source) {
			extension.addProvider(new AniyomiProvider(source));
		} else if(main instanceof AnimeSourceFactory factory) {
			for(var source : factory.createSources()) {
				fillProvider(extension, source);
			}
		} else {
			Log.e("AniyomiManager", "Unknown source type: " + main.getClass().getName());
		}
	}
}