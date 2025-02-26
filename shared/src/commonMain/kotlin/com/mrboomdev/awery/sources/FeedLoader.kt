package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.constants.AweryFilters
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.module.CatalogModule
import com.mrboomdev.awery.ext.util.GlobalId
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.channelFlow

fun List<CatalogFeed>.loadAll() = channelFlow {
	for(feed in this@loadAll) {
		feed.load(this@channelFlow)
	}
}

@Suppress("ConvertCallChainIntoSequence")
private suspend fun CatalogFeed.load(channel: SendChannel<CatalogFeed.Loaded>) {
	if(managerId == "INTERNAL") {
		when(feedId) {
			"AUTO_GENERATE" -> ExtensionsManager.allSources
				.map { it.createModules() }
				.flatten()
				.filterIsInstance<CatalogModule>()
				.map { it.createFeeds() }
				.flatten()
				.shuffled()
				.forEach { it.load(channel) }
			
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
			
			else -> throw IllegalArgumentException("Unknown feed! $feedId")
		}
		
		return
	}
	
	if(managerId == null) {
		return channel.send(
			CatalogFeed.Loaded(
				feed = this,
				throwable = UnsupportedOperationException("The feed cannot be loaded because no managerId was specified!")
			)
		)
	}
	
	if(sourceId == null) {
		return channel.send(
			CatalogFeed.Loaded(
				feed = this,
				throwable = UnsupportedOperationException("The feed cannot be loaded because no sourceId was specified!")
			)
		)
	}
	
	val source = ExtensionsManager.getSource(GlobalId(managerId!!, sourceId!!)).let {
		it ?: return channel.send(
			CatalogFeed.Loaded(
				feed = this,
				throwable = ZeroResultsException("Source isn't installed! $it")
			))
	}
	
	if(source !is Source) {
		return channel.send(
			CatalogFeed.Loaded(
			feed = this,
			throwable = UnsupportedOperationException("This isn't an source!")
		))
	}
	
	if(!source.context.isEnabled) {
		return
	}
	
	try {
		channel.send(CatalogFeed.Loaded(
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
		channel.send(
			CatalogFeed.Loaded(
			feed = this,
			throwable = t
		))
	}
}