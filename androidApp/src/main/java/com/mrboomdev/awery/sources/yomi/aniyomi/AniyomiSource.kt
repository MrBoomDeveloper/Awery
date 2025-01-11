package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.R
import com.mrboomdev.awery.ext.AndroidImage
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.data.get
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.generated.Res
import com.mrboomdev.awery.generated.no_media_found
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.YomiSource
import com.mrboomdev.awery.util.extensions.mapOfNotNull
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AniyomiSource(
	packageInfo: PackageInfo,
	isEnabled: Boolean,
	manager: YomiManager<*>,
	ageRating: AweryAgeRating?,
	name: String,
	exception: Throwable?,
	icon: AndroidImage,
	val source: AnimeSource?
) : YomiSource(
	packageInfo = packageInfo,
	isEnabled = isEnabled,
	manager = manager,
	name = name,
	ageRating = ageRating,
	icon = icon,
	exception = exception,

	features = mutableListOf<AweryFeature>().apply {
		if(source is AnimeCatalogueSource) {
			add(AweryFeature.FEEDS)
			add(AweryFeature.SEARCH_MEDIA)
		}

		if(source is ConfigurableAnimeSource) {
			add(AweryFeature.CUSTOM_SETTINGS)
		}
	}.toTypedArray()
) {

	override val feeds = if(source is AnimeCatalogueSource) {
		CatalogSearchResults(listOfNotNull(
			CatalogFeed(
				managerId = AniyomiManager.ID,
				sourceId = context.id,
				feedId = FEED_POPULAR,
				title = "Popular in ${source.name}"
			),

			if(!source.supportsLatest) null else CatalogFeed(
				managerId = AniyomiManager.ID,
				sourceId = context.id,
				feedId = FEED_LATEST,
				title = "Latest in ${source.name}"
			)
		))
	} else null

	private suspend fun getAnimesPage(filters: List<Setting>): AnimesPage {
		return withContext(Dispatchers.IO) {
			if(source is AnimeCatalogueSource) {
				val feed = filters[FILTER_FEED]?.value as? String
				val query = filters[FILTER_QUERY]?.value as? String ?: ""
				val page = filters[FILTER_PAGE]?.value as? Int ?: 0

				return@withContext when(feed) {
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
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun <E, T : Catalog<E>> search(
		catalog: T, filters: List<Setting>
	): CatalogSearchResults<E> {
		return when(catalog) {
			is Catalog.Media -> getAnimesPage(filters).let { page ->
				if(page.animes.isEmpty()) {
					throw ZeroResultsException("Zero media results!", i18n(Res.string.no_media_found))
				}

				CatalogSearchResults(page.animes.map { anime ->
					CatalogMedia(
						globalId = "${AniyomiManager.ID};;;${context.id};;;${anime.url}",
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
			} as CatalogSearchResults<E>

			else -> throw UnsupportedOperationException("Unsupported type!")
		}
	}
}