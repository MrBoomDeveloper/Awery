package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.constants.AweryFilters
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Settings
import com.mrboomdev.awery.sources.yomi.YomiSource
import com.mrboomdev.awery.util.extensions.mapOfNotNull
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource

abstract class AniyomiSource(
	packageInfo: PackageInfo,
	val source: AnimeSource?
) : YomiSource(packageInfo) {

	override val feeds = if(source is AnimeCatalogueSource) {
		CatalogSearchResults(listOfNotNull(
			CatalogFeed(FEED_POPULAR, "Popular in ${source.name}"),

			if(!source.supportsLatest) null
			else CatalogFeed(FEED_LATEST, "Latest in ${source.name}")
		))
	} else null

	override val features = mutableListOf<AweryFeature>().apply {
		if(source is AnimeCatalogueSource) {
			add(AweryFeature.FEEDS)
			add(AweryFeature.SEARCH_MEDIA)
		}

		if(source is ConfigurableAnimeSource) {
			add(AweryFeature.CUSTOM_SETTINGS)
		}
	}.toTypedArray()

	private suspend fun getAnimesPage(filters: Settings): AnimesPage {
		if(source is AnimeCatalogueSource) {
			val feed = filters[AweryFilters.FEED]?.value as? String
			val query = filters[AweryFilters.QUERY]?.value as? String ?: ""
			val page = filters[AweryFilters.PAGE]?.value as? Int ?: 0

			return when(feed) {
				FEED_POPULAR -> source.getPopularAnime(page)
				FEED_LATEST -> source.getLatestUpdates(page)

				else -> {
					// TODO: Apply filters
					source.getSearchAnime(page, query, source.getFilterList())
				}
			}
		}

		throw UnsupportedOperationException("This source doesn't support browsing!")
	}

	override suspend fun searchMedia(filters: Settings): CatalogSearchResults<CatalogMedia> {
		return getAnimesPage(filters).let { page ->
			CatalogSearchResults(page.animes.map { anime ->
				CatalogMedia(
					globalId = "${AniyomiManager.ID};;;$id;;;${anime.url}",
					titles = arrayOf(anime.title),
					description = anime.description,
					poster = anime.thumbnail_url,
					extra = anime.url,
					type = CatalogMedia.Type.TV,

					genres = anime.genre?.split(", ")?.toTypedArray(),

					url = if(source is AnimeHttpSource) {
						concatLink(source.baseUrl, anime.url)
					} else null,

					status = when(anime.status) {
						SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> CatalogMedia.Status.COMPLETED
						SAnime.ON_HIATUS -> CatalogMedia.Status.PAUSED
						SAnime.ONGOING -> CatalogMedia.Status.ONGOING
						SAnime.CANCELLED -> CatalogMedia.Status.CANCELLED
						else -> null
					},

					authors = mapOfNotNull(
						"Artist" to anime.artist,
						"Author" to anime.author
					)
				)
			}, page.hasNextPage)
		}
	}
}