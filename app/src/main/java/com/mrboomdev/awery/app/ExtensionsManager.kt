package com.mrboomdev.awery.app

import android.content.Context
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.data.Settings
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.tachiyomi.TachiyomiManager
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

object ExtensionsManager {
	private val managers = mutableListOf<SourcesManager<*>>()

	fun init(context: Context) = channelFlow {
		YomiManager.initYomiShit(context)

		val maxValues = mutableMapOf<SourcesManager<*>, Long>()
		val progresses = mutableMapOf<SourcesManager<*>, Long>()
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
			managers.map { manager ->
				// This implementation lets us know a total number of
				// installed extensions before even loading all of them.
				manager to manager.loadAll().also { pendingTask ->
					pendingTask.size?.also {
						maxValues[manager] = it
						progress.max = maxValues.values.sum()
					}
				}.data
			}.forEach {(manager, task) ->
				task.onEach {
					if(manager !in maxValues) {
						maxValues[manager] = it.max
						progress.max = maxValues.values.sum()
					}

					progresses[manager] = it.value
					progress.value = progresses.values.sum()
					send(progress)
				}.collect()
			}

			progress.finish()
			send(progress)
		}
	}

	fun List<CatalogFeed>.loadAll() = channelFlow {
		for(feed in this@loadAll) {
			feed.load(this@channelFlow)
		}
	}

	private suspend fun CatalogFeed.load(channel: SendChannel<CatalogFeed.Loaded>) {
		if(managerId == "INTERNAL") {
			when(feedId) {
				"AUTO_GENERATE" -> managers.map { it.getAll() }
					.flatten()
					.map { it.getFeeds() }
					.flatten()
					.shuffled()
					.forEach { it.load(channel) }

				"BOOKMARKS" -> {
					for(list in database.listDao.all) {
						val media = database.mediaProgressDao.getAllFromList(list.id)

						if(media.isEmpty()) {
							channel.send(CatalogFeed.Loaded(
								throwable = ZeroResultsException("No bookmarks", R.string.no_media_found),
								feed = CatalogFeed(
									title = list.name,
									hideIfEmpty = true
								)
							))

							continue
						}

						channel.send(CatalogFeed.Loaded(
							items = CatalogSearchResults(media.map {
								database.mediaDao.get(it.globalId).toCatalogMedia()
							}),

							feed = CatalogFeed(
								style = CatalogFeed.Style.ROW,
								title = list.name,
								hideIfEmpty = true
							)
						))
					}
				}

				else -> throw IllegalArgumentException("Unknown feed! $feedId")
			}

			return
		}

		val manager = managerId?.let {
			getManager(it) ?: return channel.send(CatalogFeed.Loaded(
				feed = this,
				throwable = ZeroResultsException("Source manager isn't installed! $it")
			))
		} ?: return channel.send(CatalogFeed.Loaded(
			feed = this,
			throwable = UnsupportedOperationException("The feed cannot be loaded because no managerId was specified!")
		))

		val source = sourceId?.let {
			manager[it] ?: return channel.send(CatalogFeed.Loaded(
				feed = this,
				throwable = ZeroResultsException("Source isn't installed! $it")
			))
		} ?: return channel.send(CatalogFeed.Loaded(
			feed = this,
			throwable = UnsupportedOperationException("The feed cannot be loaded because no sourceId was specified!")
		))

		if(!source.isEnabled) {
			return
		}

		try {
			channel.send(CatalogFeed.Loaded(
				feed = this,
				items = source.search(Source.Catalog.Media, Settings(
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
			channel.send(CatalogFeed.Loaded(
				feed = this,
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