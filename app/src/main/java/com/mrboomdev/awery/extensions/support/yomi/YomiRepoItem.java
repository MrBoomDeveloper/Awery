package com.mrboomdev.awery.extensions.support.yomi;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YomiRepoItem {
	private static final String FILE_NAME = "index.min.json";
	public List<Source> sources;
	public String name, pkg, apk, lang, version;
	public int nsfw;

	public __Extension toExtension(YomiManager manager, @NonNull String repoUrl) {
		var baseUrl = repoUrl.substring(0, repoUrl.indexOf(FILE_NAME));
		var iconUrl = baseUrl + "icon/" + pkg + ".png";
		var apkUrl = baseUrl + "apk/" + apk;

		var ext = new __Extension(pkg, name, version, apkUrl) {
			@Override
			public boolean isNsfw() {
				return nsfw == 1;
			}

			@Override
			public String getRawIcon() {
				return iconUrl;
			}
		};

		for(var source : sources) {
			var features = new HashSet<>(manager.getBaseFeatures());

			ext.addProvider(new __ExtensionProvider() {
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