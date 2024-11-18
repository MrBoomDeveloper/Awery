package com.mrboomdev.awery.sources.yomi.tachiyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AweryFeature
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class TachiyomiSource(
	packageInfo: PackageInfo,
	val source: MangaSource?
): YomiSource(packageInfo) {

	override val feeds = if(source is CatalogueSource) {
		CatalogSearchResults(listOfNotNull(
			CatalogFeed(
				managerId = TachiyomiManager.ID,
				sourceId = id,
				feedId = FEED_POPULAR,
				title = "Popular in ${source.name}"
			),

			if(!source.supportsLatest) null else CatalogFeed(
				managerId = TachiyomiManager.ID,
				sourceId = id,
				feedId = FEED_LATEST,
				title = "Latest in ${source.name}"
			)
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
		return withContext(Dispatchers.IO) {
			if(source is CatalogueSource) {
				val feed = filters[FILTER_FEED]?.value as? String
				val query = filters[FILTER_QUERY]?.value as? String ?: ""
				val page = filters[FILTER_PAGE]?.value as? Int ?: 0

				return@withContext when(feed) {
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
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun <E, T : Catalog<E>> search(catalog: T, filters: Settings): CatalogSearchResults<E> {
		return when(catalog) {
			Catalog.Media -> getMangasPage(filters).let { page ->
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
			} as CatalogSearchResults<E>

			else -> throw UnsupportedOperationException("Unsupported catalog! $catalog")
		}
	}
}