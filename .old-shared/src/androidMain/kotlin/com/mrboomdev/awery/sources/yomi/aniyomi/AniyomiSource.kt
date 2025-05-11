package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.data.settings.get
import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.constants.AweryFilters
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.source.module.CatalogModule
import com.mrboomdev.awery.ext.source.module.Module
import com.mrboomdev.awery.ext.source.module.SettingsModule
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
	exception = exception
) {
	
	override fun createModules(): List<Module> = listOfNotNull(
		if(source is AnimeCatalogueSource) {
			object : CatalogModule {
				override suspend fun createFeeds() = CatalogSearchResults(listOfNotNull(
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
				
				override suspend fun search(filters: List<Setting>) = getAnimesPage(filters).let { page ->
					if(page.animes.isEmpty()) {
						throw ZeroResultsException("Zero media results!", i18n(Res.string.no_media_found))
					}
						
					CatalogSearchResults(page.animes.map { anime -> 
						anime.toMedia(this@AniyomiSource) 
					}, page.hasNextPage)
				}
			}
		} else null,
		
		if(source is ConfigurableAnimeSource) {
			object : SettingsModule {
				override fun getSettings(): List<Setting> {
					TODO("Not yet implemented")
				}
			}
		} else null
	)

	private suspend fun getAnimesPage(filters: List<Setting>): AnimesPage {
		if(source !is AnimeCatalogueSource) {
			throw UnsupportedOperationException("This source doesn't support browsing!")
		}
		
		return withContext(Dispatchers.IO) {
			val feed = filters[AweryFilters.FEED]?.value as? String
			val query = filters[AweryFilters.QUERY]?.value as? String ?: ""
			val page = filters[AweryFilters.PAGE]?.value as? Int ?: 0
			
			return@withContext when(feed) {
				FEED_POPULAR -> source.getPopularAnime(page)
				FEED_LATEST -> source.getLatestUpdates(page)

				else -> {
					// TODO: Apply filters
					source.getSearchAnime(page, query, source.getFilterList())
				}
			}
		}
	}
}