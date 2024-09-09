package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import eu.kanade.tachiyomi.animesource.AnimeSource;
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory;

public class AniyomiManager extends YomiManager {
	public static final String MANAGER_ID = "ANIYOMI_KOTLIN";

	private static final Set<String> BASE_FEATURES = Set.of(
			ExtensionConstants.FEATURE_MEDIA_WATCH,
			ExtensionConstants.FEATURE_MEDIA_SEARCH);

	@Override
	public @NotNull String getName() {
		return "Aniyomi";
	}

	@Override
	public @NotNull String getId() {
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
	public Set<String> getBaseFeatures() {
		return BASE_FEATURES;
	}

	@Override
	public List<? extends __ExtensionProvider> createProviders(__Extension extension, Object main) {
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