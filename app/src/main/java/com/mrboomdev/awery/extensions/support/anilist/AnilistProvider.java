package com.mrboomdev.awery.extensions.support.anilist;

import static com.mrboomdev.awery.data.Constants.ANILIST_EXTENSION_ID;
import static com.mrboomdev.awery.util.NiceUtils.findIn;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.support.anilist.data.AnilistMedia;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistQuery;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistSearchQuery;
import com.mrboomdev.awery.sdk.data.CatalogFilter;

import java.util.Collection;
import java.util.List;

/**
 * A temporary extension provider to adapt source-based search more quickly
 */
public class AnilistProvider extends ExtensionProvider {
	private static final List<Integer> FEATURES = List.of(FEATURE_MEDIA_SEARCH);
	private static AnilistProvider instance;

	public static AnilistProvider getInstance() {
		if (instance == null) {
			instance = new AnilistProvider(null, null);
		}

		return instance;
	}

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
			@Nullable List<CatalogFilter> filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		var queryFilter = findIn(filter -> filter.getId().equals("query"), filters);
		var pageFilter = findIn(filter -> filter.getId().equals("page"), filters);

		var adultMode = AwerySettings.getInstance().getEnum(
				AwerySettings.content.ADULT_CONTENT, AwerySettings.AdultContentMode.class);

		new AnilistSearchQuery.Builder()
				.setSearchQuery(queryFilter.getStringValue())
				.setPage(pageFilter == null ? 1 : pageFilter.getIntegerValue())
				.setIsAdult(switch(adultMode) {
					case ENABLED -> null;
					case DISABLED -> false;
					case ONLY -> true;
				})
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.SEARCH_MATCH)
				.build()
				.executeQuery(context, callback::onSuccess)
				.catchExceptions(callback::onFailure);
	}

	@Override
	public String getId() {
		return ANILIST_EXTENSION_ID;
	}
}