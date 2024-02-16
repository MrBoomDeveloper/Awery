package com.mrboomdev.awery.catalog.provider;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.provider.data.Episode;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.CoroutineUtil;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList;
import eu.kanade.tachiyomi.network.HttpException;

public class AniyomiExtensionProvider extends ExtensionProvider {
	private final AnimeCatalogueSource source;

	public AniyomiExtensionProvider(AnimeCatalogueSource source) {
		this.source = source;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<Collection<Episode>> callback) {
		var filter = new AnimeFilterList();

		new Thread(() -> CoroutineUtil.getObservableValue(source.fetchSearchAnime(0, media.titles.get(1), filter), (pag, t) -> {
			if(t != null) {
				t.printStackTrace();

				if(t instanceof SocketException || t instanceof HttpException) {
					callback.onFailure(ExtensionProvider.CONNECTION_FAILED);
					return;
				}

				callback.onFailure(t);
				return;
			}

			var animes = pag.getAnimes();

			if(animes.isEmpty()) {
				callback.onFailure(ExtensionProvider.ZERO_RESULTS);
				return;
			}

			CoroutineUtil.getObservableValue(source.fetchEpisodeList(animes.get(0)), ((episodes, _t) -> {
				if(_t != null) {
					_t.printStackTrace();
					callback.onFailure(_t);
					return;
				}

				callback.onSuccess(Objects.requireNonNull(episodes)
						.stream().map(item -> new Episode(
								media,
								item.getName(),
								item.getUrl(),
								item.getEpisode_number()
						)).collect(Collectors.toCollection(ArrayList::new)));
			}));
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