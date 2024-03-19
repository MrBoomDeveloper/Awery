package com.mrboomdev.awery.extensions.support.yomi.tachiyomi;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import eu.kanade.tachiyomi.source.CatalogueSource;
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
	public List<ExtensionProvider> createProviders(Extension extension, Object main) {
		if(main instanceof CatalogueSource source) {
			return List.of(new TachiyomiProvider(source));
		} else if(main instanceof SourceFactory factory) {
			return factory.createSources().stream()
					.map(source -> createProviders(extension, source))
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	@Override
	public int getFlags() {
		return Extension.FLAG_BOOK_EXTENSION;
	}
}