package com.mrboomdev.awery.extensions.support.internal;

import static com.mrboomdev.awery.platform.PlatformResourcesKt.i18n;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.ext.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.generated.Res;
import com.mrboomdev.awery.generated.String0_commonMainKt;
import com.mrboomdev.awery.util.async.AsyncFuture;

import java.util.Set;

public class InternalProviders {

	protected static abstract class InternalProvider extends ExtensionProvider {
		private InternalManager manager;
		private Extension extension;

		@Override
		public ExtensionsManager getManager() {
			return manager;
		}

		@Override
		public Extension getExtension() {
			return extension;
		}

		protected void setup(InternalManager manager, Extension extension) {
			this.manager = manager;
			this.extension = extension;
		}
	}

	public static class Lists extends InternalProvider {
		private final Set<String> FEATURES = Set.of(ExtensionProvider.FEATURE_MEDIA_SEARCH, FEATURE_FEEDS);

		@Override
		public AsyncFuture<CatalogSearchResults<? extends CatalogMedia>> searchMedia(@NonNull SettingsList filters) {
			return thread(() -> {
				var feed = filters.require(FILTER_FEED).getStringValue();

				var progresses = App.Companion.getDatabase().getMediaProgressDao().getAllFromList(feed);

				if(progresses.isEmpty()) {
					throw new ZeroResultsException("No bookmarks", i18n(String0_commonMainKt.getNo_media_found(Res.string.INSTANCE)));
				}

				return CatalogSearchResults.of(stream(progresses)
						.map(progress -> App.Companion.getDatabase().getMediaDao()
								.get(progress.globalId).toCatalogMedia())
						.toList(), false);
			});
		}

		@Override
		public Set<String> getFeatures() {
			return FEATURES;
		}

		@Override
		public String getId() {
			return "BOOKMARKS";
		}

		@Override
		public AdultContent getAdultContentMode() {
			return AdultContent.HIDDEN;
		}
	}
}