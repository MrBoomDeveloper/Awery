package com.mrboomdev.awery.extensions.support.internal;

import static com.mrboomdev.awery.app.data.db.AweryDB.getDatabase;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import java.util.Set;

public class InternalProviders {

	protected static abstract class InternalProvider extends __ExtensionProvider {
		private InternalManager manager;
		private __Extension extension;

		@Override
		public ExtensionsManager getManager() {
			return manager;
		}

		@Override
		public __Extension getExtension() {
			return extension;
		}

		protected void setup(InternalManager manager, __Extension extension) {
			this.manager = manager;
			this.extension = extension;
		}
	}

	public static class Lists extends InternalProvider {
		private final Set<String> FEATURES = Set.of(
				ExtensionConstants.FEATURE_MEDIA_SEARCH,
				ExtensionConstants.FEATURE_FEEDS);

		@Override
		public AsyncFuture<CatalogSearchResults<? extends CatalogMedia>> searchMedia(@NonNull SettingsList filters) {
			return thread(() -> {
				var feed = filters.require(ExtensionConstants.FILTER_FEED).getStringValue();

				var progresses = getDatabase().getMediaProgressDao().getAllFromList(feed);

				if(progresses.isEmpty()) {
					throw new ZeroResultsException("No bookmarks", R.string.no_media_found);
				}

				return CatalogSearchResults.of(stream(progresses)
						.map(progress -> getDatabase().getMediaDao()
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