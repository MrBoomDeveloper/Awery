package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;

import java.util.Collections;
import java.util.List;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory;

public class AniyomiManager extends YomiManager {
	protected static final String TYPE_ID = "ANIYOMI_KOTLIN";

	@Override
	public String getName() {
		return "Aniyomi";
	}

	@Override
	public String getId() {
		return TYPE_ID;
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
	public List<? extends ExtensionProvider> createProviders(Extension extension, Object main) {
		if(main instanceof AnimeCatalogueSource source) {
			return List.of(new AniyomiProvider(this, extension, source));
		} else if(main instanceof AnimeSourceFactory factory) {
			return stream(factory.createSources())
					.map(source -> source instanceof AnimeCatalogueSource catalogueSource
							? new AniyomiProvider(this, extension, catalogueSource, true) : null)
					.filter(item -> {
						if(item == null) {
							toast("Failed to create Aniyomi provider");
							return false;
						}

						return true;
					}).toList();
		}

		toast("Failed to create Aniyomi provider");
		return Collections.emptyList();
	}
}