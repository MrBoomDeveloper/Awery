package com.mrboomdev.awery.sources.yomi.tachiyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.constants.AweryFilters
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Settings
import com.mrboomdev.awery.sources.yomi.YomiSource
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.util.extensions.awaitSingle
import com.mrboomdev.awery.util.extensions.mapOfNotNull
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.MangaSource
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource

abstract class TachiyomiSource(
	packageInfo: PackageInfo,
	val source: MangaSource?
): YomiSource(packageInfo) {

	override val feeds = if(source is CatalogueSource) {
		CatalogSearchResults(listOfNotNull(
			CatalogFeed(FEED_POPULAR, "Popular in ${source.name}"),

			if(!source.supportsLatest) null
			else CatalogFeed(FEED_LATEST, "Latest in ${source.name}")
		))
	} else null

	override val features = mutableListOf<AweryFeature>().apply {
		if(source is CatalogueSource) {
			add(AweryFeature.FEEDS)
			add(AweryFeature.SEARCH_MEDIA)
		}

		if(source is ConfigurableSource) {
			add(AweryFeature.CUSTOM_SETTINGS)
		}
	}.toTypedArray()

	private suspend fun getMangasPage(filters: Settings): MangasPage {
		if(source is CatalogueSource) {
			val feed = filters[AweryFilters.FEED]?.value as? String
			val query = filters[AweryFilters.QUERY]?.value as? String ?: ""
			val page = filters[AweryFilters.PAGE]?.value as? Int ?: 0

			return when(feed) {
				FEED_POPULAR -> source.fetchPopularManga(page)
				FEED_LATEST -> source.fetchLatestUpdates(page)

				else -> {
					// TODO: Apply filters
					source.fetchSearchManga(page, query, source.getFilterList())
				}
			}.awaitSingle()
		}

		throw UnsupportedOperationException("This source doesn't support browsing!")
	}

	override suspend fun searchMedia(filters: Settings): CatalogSearchResults<CatalogMedia> {
		return getMangasPage(filters).let { page ->
			CatalogSearchResults(page.mangas.map { manga ->
				CatalogMedia(
					globalId = "${AniyomiManager.ID};;;$id;;;${manga.url}",
					titles = arrayOf(manga.title),
					description = manga.description,
					poster = manga.thumbnail_url,
					extra = manga.url,
					type = CatalogMedia.Type.BOOK,

					genres = manga.genre?.split(", ")?.toTypedArray(),

					url = if(source is HttpSource) {
						concatLink(source.baseUrl, manga.url)
					} else null,

					status = when(manga.status) {
						SManga.COMPLETED, SManga.PUBLISHING_FINISHED -> CatalogMedia.Status.COMPLETED
						SManga.ON_HIATUS -> CatalogMedia.Status.PAUSED
						SManga.ONGOING -> CatalogMedia.Status.ONGOING
						SManga.CANCELLED -> CatalogMedia.Status.CANCELLED
						else -> null
					},

					authors = mapOfNotNull(
						"Artist" to manga.artist,
						"Author" to manga.author
					)
				)
			}, page.hasNextPage)
		}
	}
}