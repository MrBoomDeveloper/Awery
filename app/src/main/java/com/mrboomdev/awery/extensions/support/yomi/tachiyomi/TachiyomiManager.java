package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

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

import eu.kanade.tachiyomi.source.MangaSource;
import eu.kanade.tachiyomi.source.SourceFactory;

public class TachiyomiManager extends YomiManager {
	private static final Set<String> BASE_FEATURES = Set.of(
			ExtensionConstants.FEATURE_MEDIA_READ,
			ExtensionConstants.FEATURE_MEDIA_SEARCH);

	@Override
	public @NotNull String getName() {
		return "Tachiyomi";
	}

	@Override
	public @NotNull String getId() {
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
	public Set<String> getBaseFeatures() {
		return BASE_FEATURES;
	}

	@Override
	public List<? extends __ExtensionProvider> createProviders(__Extension extension, Object main) {
		if(main instanceof MangaSource source) {
			return List.of(new TachiyomiProvider(extension, source, false) {
				@Override
				public YomiManager getManager() {
					return TachiyomiManager.this;
				}
			});
		} else if(main instanceof SourceFactory factory) {
			return stream(factory.createSources())
					.map(source -> new TachiyomiProvider(extension, source, true) {
						@Override
						public YomiManager getManager() {
							return TachiyomiManager.this;
						}
					})
					.toList();
		}

		toast("Failed to create an Tachiyomi provider!");
		return Collections.emptyList();
	}
}