package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.data.settings.get
import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.util.Image
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.YomiSource
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AniyomiSource(
	packageInfo: PackageInfo,
	isEnabled: Boolean,
	manager: YomiManager<*>,
	ageRating: AgeRating?,
	name: String,
	exception: Throwable?,
	icon: Image,
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

				CatalogSearchResults(page.animes.map { anime -> anime.toMedia(this) }, page.hasNextPage)
			} as CatalogSearchResults<E>

			else -> throw UnsupportedOperationException("Unsupported type!")
		}
	}
}