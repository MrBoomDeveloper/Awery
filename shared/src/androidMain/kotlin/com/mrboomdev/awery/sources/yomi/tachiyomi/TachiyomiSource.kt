package com.mrboomdev.awery.sources.yomi.tachiyomi

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
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.util.awaitSingle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TachiyomiSource(
	packageInfo: PackageInfo,
	isEnabled: Boolean,
	manager: YomiManager<*>,
	ageRating: AgeRating?,
	name: String,
	exception: Throwable?,
	icon: Image,
	val source: Source?
): YomiSource(
	packageInfo = packageInfo,
	isEnabled = isEnabled,
	manager = manager,
	name = name,
	ageRating = ageRating,
	icon = icon,
	exception = exception,

	features = mutableListOf<AweryFeature>().apply {
		if(source is CatalogueSource) {
			add(AweryFeature.FEEDS)
			add(AweryFeature.SEARCH_MEDIA)
		}

		if(source is ConfigurableSource) {
			add(AweryFeature.CUSTOM_SETTINGS)
		}
	}.toTypedArray()
) {

	override val feeds = if(source is CatalogueSource) {
		CatalogSearchResults(listOfNotNull(
			CatalogFeed(
				managerId = TachiyomiManager.ID,
				sourceId = context.id,
				feedId = FEED_POPULAR,
				title = "Popular in ${source.name}"
			),

			if(!source.supportsLatest) null else CatalogFeed(
				managerId = TachiyomiManager.ID,
				sourceId = context.id,
				feedId = FEED_LATEST,
				title = "Latest in ${source.name}"
			)
		))
	} else null

	private suspend fun getMangasPage(filters: List<Setting>): MangasPage {
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
	override suspend fun <E, T : Catalog<E>> search(
		catalog: T, filters: List<Setting>
	): CatalogSearchResults<E> {
		return when(catalog) {
			Catalog.Media -> getMangasPage(filters).let { page ->
				if(page.mangas.isEmpty()) {
					throw ZeroResultsException("Zero media results!", i18n(Res.string.no_media_found))
				}

				CatalogSearchResults(page.mangas.map { manga -> manga.toMedia(this) }, page.hasNextPage)
			} as CatalogSearchResults<E>

			else -> throw UnsupportedOperationException("Unsupported catalog! $catalog")
		}
	}
}