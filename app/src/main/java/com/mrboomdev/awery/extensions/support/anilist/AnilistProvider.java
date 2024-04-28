package com.mrboomdev.awery.extensions.support.anilist;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistSearchQuery;

import java.util.Collection;
import java.util.List;

/**
 * A temporary extension provider to adapt source-based search more quickly
 */
public class AnilistProvider extends ExtensionProvider {
	private static final List<Integer> FEATURES = List.of(FEATURE_MEDIA_SEARCH);

	public AnilistProvider(ExtensionsManager manager, Extension extension) {
		super(manager, extension);
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public void searchMedia(
			Context context,
			@Nullable List<CatalogFilter> filter,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		new AnilistSearchQuery.Builder().build()
				.executeQuery(context, callback::onSuccess)
				.catchExceptions(callback::onFailure);
	}

	@Override
	public String getId() {
		return AweryApp.ANILIST_EXTENSION_ID;
	}
}