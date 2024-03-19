package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionProviderChild;
import com.mrboomdev.awery.extensions.ExtensionProviderGroup;
import com.mrboomdev.awery.extensions.support.yomi.YomiManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
	public List<ExtensionProvider> createProviders(Extension extension, Object main) {
		return createProviders(extension, main, false);
	}

	private List<ExtensionProvider> createProviders(Extension extension, Object main, boolean forParent) {
		if(main instanceof AnimeCatalogueSource source) {
			return List.of(forParent
					? new ChildProvider(source)
					: new AniyomiProvider(source));
		}

		if(main instanceof AnimeSourceFactory factory) {
			var providers = factory.createSources().stream()
					.map(source -> createProviders(extension, source, true))
					.flatMap(Collection::stream)
					.collect(Collectors.toList());

			var parent = new ExtensionProviderGroup(extension.getName(), providers);

			for(var source : providers) {
				((ChildProvider) source).setParent(parent);
			}

			return List.of(parent);
		}

		return Collections.emptyList();
	}

	private static class ChildProvider extends AniyomiProvider implements ExtensionProviderChild {
		private ExtensionProviderGroup parent;

		public ChildProvider(AnimeCatalogueSource source) {
			super(source);
		}

		public void setParent(ExtensionProviderGroup parent) {
			this.parent = parent;
		}

		@Override
		public ExtensionProviderGroup getProviderParent() {
			return parent;
		}
	}

	@Override
	public int getFlags() {
		return Extension.FLAG_VIDEO_EXTENSION;
	}
}