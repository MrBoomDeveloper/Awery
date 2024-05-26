package com.mrboomdev.awery.extensions.support.yomi;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
			var features = new ArrayList<>(manager.getBaseFeatures());

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
				public Collection<Integer> getFeatures() {
					return features;
				}

				@Override
				public String getId() {
					return source.id;
				}
			});
		}

		return ext;
	}

	public static class Source {
		public String name, lang, id, baseUrl;
	}
}