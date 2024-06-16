package com.mrboomdev.awery.extensions.support.anilist;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.data.Constants.ANILIST_EXTENSION_ID;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.support.anilist.data.AnilistEpisode;
import com.mrboomdev.awery.extensions.support.anilist.data.AnilistMedia;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistQuery;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistSearchQuery;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.graphql.GraphQLParser;
import com.mrboomdev.awery.util.io.HttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
	public void getEpisodes(
			int page,
			@NonNull CatalogMedia media,
			@NonNull ResponseCallback<List<? extends CatalogEpisode>> callback
	) {
		var id = media.getId("anilist");

		if(id == null) {
			callback.onFailure(new ZeroResultsException("No anilist id found!", R.string.no_episodes_found));
			return;
		}

		try {
			HttpClient.post("https://graphql.anilist.co")
					.addHeader("Content-Type", "application/json")
					.addHeader("Accept", "application/json")
					.setBody(new JSONObject().put("query", """
							{
								Media(id: __ID__) {
									streamingEpisodes {
										thumbnail
									}
								}
							}
					""").toString().replace("__ID__", id), MimeTypes.JSON)
					.callAsync(getAnyContext(), (HttpClient.SimpleHttpCallback) (res, e) -> {
						if(e != null) {
							callback.onFailure(e);
							return;
						}

						if(res == null) {
							throw new IllegalStateException("Response is null!");
						}

						try {
							var anilistMedia = GraphQLParser.parse(res.getText(), AnilistMedia.class);
							var index = new AtomicInteger();

							callback.onSuccess(stream(anilistMedia.streamingEpisodes)
									.map(AnilistEpisode::toCatalogEpisode)
									.peek(item -> item.setNumber(index.incrementAndGet()))
									.toList());
						} catch(IOException ex) {
							throw new IllegalStateException("Failed to parse response!", ex);
						}
					});
		} catch(JSONException e) {
			throw new IllegalStateException("Failed to construct a request!", e);
		}
	}

	@Override
	public void searchMedia(
			Context context,
			@Nullable List<SettingsItem> filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		var query = find(filters, filter -> filter.getKey().equals(FILTER_QUERY));
		var page = find(filters, filter -> filter.getKey().equals(FILTER_PAGE));

		new AnilistSearchQuery.Builder()
				.setSearchQuery(query.getStringValue())
				.setPage(page == null ? 1 : page.getIntegerValue() + 1)
				.setIsAdult(switch(AwerySettings.ADULT_MODE.getValue(AwerySettings.AdultMode_Values.DISABLED)) {
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