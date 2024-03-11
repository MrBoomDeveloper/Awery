package com.mrboomdev.awery.catalog.extensions.support.yomi.aniyomi;

import android.util.Log;

import com.mrboomdev.awery.catalog.extensions.Extension;
import com.mrboomdev.awery.catalog.extensions.ExtensionProvider;
import com.mrboomdev.awery.catalog.extensions.support.yomi.YomiManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
	public List<ExtensionProvider> createProviders(Extension extension, Object main) {
		if(main instanceof AnimeCatalogueSource source) {
			return List.of(new AniyomiProvider(source));
		} else if(main instanceof AnimeSourceFactory factory) {
			return factory.createSources().stream()
					.map(source -> createProviders(extension, source))
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}
}