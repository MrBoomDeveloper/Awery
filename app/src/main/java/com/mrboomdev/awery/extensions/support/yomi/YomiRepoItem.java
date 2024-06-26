package com.mrboomdev.awery.extensions.support.yomi;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YomiRepoItem {
	public List<Source> sources;
	public String name, pkg, apk, lang, version;
	public int nsfw;

	public Extension toExtension(YomiManager manager) {
		var ext = new Extension(pkg, name, version, apk) {
			@Override
			public boolean isNsfw() {
				return nsfw == 1;
			}
		};

		for(var source : sources) {
			var features = new HashSet<>(manager.getBaseFeatures());

			if(nsfw == 1) {
				features.add(ExtensionProvider.FEATURE_NSFW);
			}

			ext.addProvider(new ExtensionProvider() {
				@Override
				public String getName() {
					return source.name;
				}

				@Override
				public String getLang() {
					return source.lang;
				}

				@Override
				public String getPreviewUrl() {
					return source.baseUrl;
				}

				@Override
				public ExtensionsManager getManager() {
					return null;
				}

				@Override
				public Set<String> getFeatures() {
					return features;
				}

				@Override
				public String getId() {
					return source.id;
				}

				@Override
				public AdultContent getAdultContentMode() {
					return nsfw == 1 ? AdultContent.ONLY : AdultContent.NONE;
				}
			});
		}

		return ext;
	}

	public static class Source {
		public String name, lang, id, baseUrl;
	}
}