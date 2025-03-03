package com.mrboomdev.awery.sources

import com.mrboomdev.awery.data.AweryAppFilters
import com.mrboomdev.awery.ext.constants.AweryFilters
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.module.CatalogModule
import com.mrboomdev.awery.ext.util.GlobalId
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.utils.createLogger
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.channelFlow

private val logger = createLogger()

class FeedLoadException(val feed: CatalogFeed, cause: Throwable): Exception(cause)

class LoadedFeed(
	val feed: CatalogFeed,
	val items: CatalogSearchResults<CatalogMedia>? = null,
	val throwable: Throwable? = null
)

/**
 * Transforms list of app-specific filters into a list of simple to use filters without any sugar.
 */
suspend fun List<CatalogFeed>.processFeeds(): List<CatalogFeed> = flatMap { feed ->
	when(feed.managerId) {
		AweryAppFilters.PROCESSOR_MANAGER -> when(feed.feedId) {
			AweryAppFilters.FEED_AUTOGENERATE -> 
				ExtensionsManager.getAllModules<CatalogModule>()
					.flatMap { module -> module.createFeeds() }
				
//			AweryAppFilters.FEED_BOOKMARKS -> 
//			AweryAppFilters.FEED_CONTINUE ->
			
//			"BOOKMARKS" -> {
//				for(list in AweryDB.instance.getListDao().getAll()) {
//					val media = AweryDB.instance.getMediaProgressDao().getAllFromList(list.id)
//					
//					if(media.isEmpty()) {
//						channel.send(
//							CatalogFeed.Loaded(
//							throwable = ZeroResultsException("No bookmarks", i18n(Res.string.no_media_found)),
//							feed = CatalogFeed(
//								title = list.name,
//								hideIfEmpty = true
//							)
//						))
//						
//						continue
//					}
//					
//					channel.send(
//						CatalogFeed.Loaded(
//						items = CatalogSearchResults(media.map {
//							AweryDB.instance.getMediaDao().get(it.globalId)
//						}),
//						
//						feed = CatalogFeed(
//							style = CatalogFeed.Style.ROW,
//							title = list.name,
//							hideIfEmpty = true
//						)
//					))
//				}
//			}
					
			else -> {
				logger.w("Unknown processed feed! ${feed.feedId}")
				emptyList()
			}
		}
			
		else -> listOf(feed)
	}
}

fun List<CatalogFeed>.loadAll() = channelFlow {
	for(feed in this@loadAll) {
		feed.load(this@channelFlow)
	}
}

@Suppress("ConvertCallChainIntoSequence")
private suspend fun CatalogFeed.load(channel: SendChannel<LoadedFeed>) {
	if(managerId == null) {
		return channel.send(
			LoadedFeed(
				feed = this,
				throwable = UnsupportedOperationException(
					"The feed cannot be loaded because no managerId was specified!")
			)
		)
	}
	
	if(sourceId == null) {
		return channel.send(
			LoadedFeed(
				feed = this,
				throwable = UnsupportedOperationException(
					"The feed cannot be loaded because no sourceId was specified!")
			)
		)
	}
	
	val source = ExtensionsManager.getSource(GlobalId(managerId!!, sourceId!!)).let {
		it ?: return channel.send(
			LoadedFeed(
				feed = this,
				throwable = ZeroResultsException("Source isn't installed! $it")
			))
	}
	
	if(source !is Source) {
		return channel.send(LoadedFeed(
			feed = this,
			throwable = UnsupportedOperationException("This isn't an source!")
		))
	}
	
	if(!source.context.isEnabled) {
		return
	}
	
	try {
		channel.send(LoadedFeed(
			feed = this,
			items = source.createModules()
				.filterIsInstance<CatalogModule>()
				.map { it.search(listOf(
					object : Setting() {
						override val key = AweryFilters.FEED
						override var value: Any? = this@load
					},
						
					object : Setting() {
						override val key = AweryFilters.PAGE
						override var value: Any? = 0
					}
				)
			) }.flatten().let { CatalogSearchResults(it) }
		))
	} catch(t: Throwable) {
		channel.send(LoadedFeed(
			feed = this,
			throwable = t
		))
	}
}