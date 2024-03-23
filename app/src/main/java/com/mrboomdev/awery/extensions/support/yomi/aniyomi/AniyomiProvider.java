package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.support.template.CatalogEpisode;
import com.mrboomdev.awery.extensions.support.template.CatalogFilter;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.extensions.support.template.CatalogSubtitle;
import com.mrboomdev.awery.extensions.support.template.CatalogVideo;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.model.AnimesPage;
import java9.util.stream.Collectors;
import okhttp3.Headers;

public class AniyomiProvider extends ExtensionProvider {
	private final AnimeCatalogueSource source;

	public AniyomiProvider(AnimeCatalogueSource source) {
		this.source = source;
	}

	@Override
	public void getEpisodes(
			int page,
			@NonNull CatalogMedia media,
			@NonNull ResponseCallback<List<? extends CatalogEpisode>> callback
	) {
		new Thread(() -> AniyomiKotlinBridge.getEpisodesList(source, AniyomiMedia.fromMedia(media), (episodes, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(episodes == null || episodes.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No episodes found"));
				return;
			}

			callback.onSuccess(stream(episodes)
					.map(AniyomiEpisode::new).toList());
		})).start();
	}

	@Override
	public void getVideos(@NonNull CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		new Thread(() -> AniyomiKotlinBridge.getVideosList(source, AniyomiEpisode.fromEpisode(episode), (videos, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(videos == null || videos.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No videos found"));
				return;
			}

			callback.onSuccess(stream(videos).map(item -> {
						var headers = item.getHeaders();

						var subtitles = stream(item.getSubtitleTracks()).map(track ->
								new CatalogSubtitle(track.getLang(), track.getUrl())).toList();

						return new CatalogVideo(
								item.getQuality(),
								item.getVideoUrl(),
								headers != null ? headers.toString() : "",
								subtitles
						);
					}).collect(Collectors.toCollection(ArrayList::new)));
		})).start();
	}

	@NonNull
	private Bundle getHeaders(@NonNull Headers headers) {
		var bundle = new Bundle();

		for(var header : headers) {
			bundle.putString(header.getFirst(), header.getSecond());
		}

		return bundle;
	}

	@Contract("null, _, _ -> false")
	private boolean checkSearchResults(AnimesPage page, Throwable t, ResponseCallback<List<? extends CatalogMedia>> callback) {
		if(t != null) {
			callback.onFailure(t);
			return false;
		}

		if(page == null) {
			callback.onFailure(new NullPointerException("page is null!"));
			return false;
		}

		if(page.getAnimes().isEmpty()) {
			callback.onFailure(new ZeroResultsException("Found nothing in the catalog. Try changing your query."));
			return false;
		}

		return true;
	}

	@Override
	public void search(CatalogFilter params, @NonNull ResponseCallback<List<? extends CatalogMedia>> callback) {
		var filter = source.getFilterList();

		new Thread(() -> AniyomiKotlinBridge.searchAnime(source, params.getPage(), params.getQuery(), filter, (page, t) -> {
			if(!checkSearchResults(page, t, callback)) return;

			callback.onSuccess(stream(page.getAnimes())
					.map(item -> new AniyomiMedia(this, item))
					.toList());
		})).start();
	}

	@NonNull
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getLang() {
		return source.getLang();
	}

	@Override
	public String getName() {
		return source.getName();
	}
}