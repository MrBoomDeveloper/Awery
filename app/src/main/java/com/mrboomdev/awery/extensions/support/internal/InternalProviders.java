package com.mrboomdev.awery.extensions.support.internal;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

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
		public void searchMedia(
				Context context,
				@NonNull SettingsList filters,
				@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
		) {
			var feed = filters.require(FILTER_FEED).getStringValue();

			thread(() -> {
				var progresses = getDatabase().getMediaProgressDao().getAllFromList(feed);

				if(progresses.isEmpty()) {
					callback.onFailure(new ZeroResultsException("No bookmarks", R.string.no_media_found));
					return;
				}

				callback.onSuccess(CatalogSearchResults.of(stream(progresses)
						.map(progress -> getDatabase().getMediaDao()
								.get(progress.globalId).toCatalogMedia())
						.toList(), false));
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