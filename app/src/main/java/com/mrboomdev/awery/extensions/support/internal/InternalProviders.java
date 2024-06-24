package com.mrboomdev.awery.extensions.support.internal;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import java.util.Collection;
import java.util.List;

public class InternalProviders {

	public static class Lists extends ExtensionProvider {
		private final List<Integer> FEATURES = List.of(ExtensionProvider.FEATURE_MEDIA_SEARCH, FEATURE_FEEDS);

		@Override
		public ExtensionsManager getManager() {
			return null;
		}

		@Override
		public void searchMedia(
				Context context,
				List<SettingsItem> filters,
				@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
		) {
			var feed = find(filters, filter -> filter.getKey().equals(FILTER_FEED));

			if(feed == null) {
				callback.onFailure(new IllegalArgumentException("No feed filter was found!"));
				return;
			}

			new Thread(() -> {
				var progresses = getDatabase().getMediaProgressDao().getAllFromList(feed.getStringValue());

				if(progresses.isEmpty()) {
					callback.onFailure(new ZeroResultsException("No bookmarks", R.string.no_media_found));
					return;
				}

				callback.onSuccess(CatalogSearchResults.of(stream(progresses)
						.map(progress -> getDatabase().getMediaDao()
								.get(progress.globalId).toCatalogMedia())
						.toList(), false));
			}).start();
		}

		@Override
		public Collection<Integer> getFeatures() {
			return FEATURES;
		}

		@Override
		public String getId() {
			return "BOOKMARKS";
		}
	}
}