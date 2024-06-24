package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.extensions.ExtensionProvider.FEATURE_MEDIA_SEARCH;
import static com.mrboomdev.awery.extensions.ExtensionProvider.FEATURE_MEDIA_WATCH;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.kanade.tachiyomi.animesource.AnimeSource;
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory;

public class AniyomiManager extends YomiManager {
	public static final String MANAGER_ID = "ANIYOMI_KOTLIN";
	private static final List<Integer> BASE_FEATURES = List.of(FEATURE_MEDIA_WATCH, FEATURE_MEDIA_SEARCH);

	@Override
	public String getName() {
		return "Aniyomi";
	}

	@Override
	public String getId() {
		return MANAGER_ID;
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
	public Collection<Integer> getBaseFeatures() {
		return BASE_FEATURES;
	}

	@Override
	public List<? extends ExtensionProvider> createProviders(Extension extension, Object main) {
		if(main instanceof AnimeSource source) {
			return List.of(new AniyomiProvider(extension, source) {
				@Override
				public YomiManager getManager() {
					return AniyomiManager.this;
				}
			});
		} else if(main instanceof AnimeSourceFactory factory) {
			return stream(factory.createSources())
					.map(source -> new AniyomiProvider(extension, source, true) {
						@Override
						public YomiManager getManager() {
							return AniyomiManager.this;
						}
					})
					.toList();
		}

		toast("Failed to create an Aniyomi provider!");
		return Collections.emptyList();
	}
}