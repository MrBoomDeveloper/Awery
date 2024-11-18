package com.mrboomdev.awery.app

import android.content.Context
import com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.data.Settings
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.tachiyomi.TachiyomiManager
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlin.reflect.KClass

object ExtensionsManager {
	private val managers = mutableListOf<SourcesManager<*>>()

	fun init(context: Context) = channelFlow {
		YomiManager.initYomiShit(context)

		val maxValues = mutableMapOf<SourcesManager<*>, Long>()
		val progress = Progress()

		managers += listOf(
			AniyomiManager(context),
			TachiyomiManager(context),
			//CloudstreamManager(),
			//LnReaderManager(),
			//KaguyaManager(),
			//MiruManager()
		)

		coroutineScope {
			for(manager in managers) {
				manager.loadAll().onEach {
					maxValues[manager] = it.max
					progress.max = maxValues.values.sum()
					progress.increment()
					send(progress)
				}.collect()
			}

			progress.isCompleted = true
			send(progress)
		}
	}

	fun List<CatalogFeed>.loadAll() = channelFlow {
		for(feed in this@loadAll) {
			loadFeed(feed)
		}
	}

	private suspend fun ProducerScope<CatalogFeed.Loaded>.loadFeed(feed: CatalogFeed) {
		if(feed.managerId == "INTERNAL") {
			when(feed.feedId) {
				"AUTO_GENERATE" -> {
					val feeds = managers.map { it.getAll() }.flatten().map { it.getFeeds() }.flatten().shuffled()

					for(gotFeed in feeds) {
						loadFeed(gotFeed)
					}
				}

				"BOOKMARKS" -> {
					TODO()
				}
			}

			return
		}

		val manager = getManager(feed.managerId)

		if(manager == null) {
			send(CatalogFeed.Loaded(
				feed = feed,
				throwable = ZeroResultsException("Source manager isn't installed! ${feed.managerId}")
			))

			return
		}

		val source = manager[feed.sourceId]

		if(source == null) {
			send(CatalogFeed.Loaded(
				feed = feed,
				throwable = ZeroResultsException("Source isn't installed! ${feed.sourceId}")
			))

			return
		}

		try {
			send(CatalogFeed.Loaded(
				feed = feed,
				items = source.search(Source.Catalog.Media, Settings(
					object : Setting() {
						override val key: String = Source.FILTER_FEED
						override var value: Any? = feed
					},

					object : Setting() {
						override val key: String = Source.FILTER_PAGE
						override var value: Any? = 0
					}
				))
			))
		} catch(t: Throwable) {
			send(CatalogFeed.Loaded(
				feed = feed,
				throwable = t
			))
		}
	}

	fun getSource(globalId: String): Source? {
		return globalId.split(";;;").let {
			// Due to compatibility we have to trim all shit after :
			getManager(it[0])?.get(it[1].split(":")[0])
		}
	}

	/**
	 * The user has decided to either enable or disable an extension.
	 * This function just writes an value to settings and doesn't load or unload an actual extension.
	 * @author MrBoomDev
	 */
	fun SourcesManager<*>.setEnabled(sourceId: String, enable: Boolean) {
		getPrefs().setValue("ext_${id}_${sourceId}", enable).saveSync()
	}

	/**
	 * Checks if the user has decided to let this extension to be enabled or not.
	 * This function just checks the settings and doesn't check if an extension is loaded or not.
	 * @author MrBoomDev
	 */
	fun SourcesManager<*>.isEnabled(sourceId: String): Boolean {
		return getPrefs().getBoolean("ext_${id}_${sourceId}", true)
	}

	fun addManager(manager: SourcesManager<*>) {
		managers.add(manager)
	}

	fun getManager(managerId: String): SourcesManager<*>? {
		return managers.find {
			managerId == it.id
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun <T: SourcesManager<*>> getManager(clazz: KClass<T>): T? {
		return managers.find {
			clazz == it::class
		} as T?
	}
}