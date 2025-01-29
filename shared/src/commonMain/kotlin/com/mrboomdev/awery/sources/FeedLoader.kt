package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.sources.ExtensionsManager.getAllManagers
import com.mrboomdev.awery.sources.ExtensionsManager.getManager
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
			"AUTO_GENERATE" -> getAllManagers().map { it.getAll() }
				.flatten()
				.filterIsInstance<Source>()
				.map { it.getFeeds() }
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
	
	val manager = managerId?.let {
		getManager(it) ?: return channel.send(
			CatalogFeed.Loaded(
			feed = this,
			throwable = ZeroResultsException("Source manager isn't installed! $it")
		))
	} ?: return channel.send(
		CatalogFeed.Loaded(
		feed = this,
		throwable = UnsupportedOperationException("The feed cannot be loaded because no managerId was specified!")
	))
	
	val source = sourceId?.let {
		manager[it] ?: return channel.send(
			CatalogFeed.Loaded(
			feed = this,
			throwable = ZeroResultsException("Source isn't installed! $it")
		))
	} ?: return channel.send(
		CatalogFeed.Loaded(
		feed = this,
		throwable = UnsupportedOperationException("The feed cannot be loaded because no sourceId was specified!")
	))
	
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
			items = source.search(Source.Catalog.Media, listOf(
				object : Setting() {
					override val key: String = Source.FILTER_FEED
					override var value: Any? = this@load
				},
				
				object : Setting() {
					override val key: String = Source.FILTER_PAGE
					override var value: Any? = 0
				}
			))
		))
	} catch(t: Throwable) {
		channel.send(
			CatalogFeed.Loaded(
			feed = this,
			throwable = t
		))
	}
}