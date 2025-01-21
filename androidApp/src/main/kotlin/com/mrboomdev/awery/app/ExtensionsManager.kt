package com.mrboomdev.awery.app

import android.annotation.SuppressLint
import android.content.Context
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.source.AbstractSource
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.PlatformPreferences
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.sources.BootstrapManager
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.channelFlow
import kotlin.reflect.KClass

object ExtensionsManager {
	@SuppressLint("StaticFieldLeak")
	private lateinit var bootstrapManager: BootstrapManager

	fun init(context: Context) = BootstrapManager(context).also {
		bootstrapManager = it
	}.onLoad()

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

				"BOOKMARKS" -> {
					for(list in database.listDao.all) {
						val media = database.mediaProgressDao.getAllFromList(list.id)

						if(media.isEmpty()) {
							channel.send(CatalogFeed.Loaded(
								throwable = ZeroResultsException("No bookmarks", i18n(Res.string.no_media_found)),
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

		if(source !is Source) {
			return channel.send(CatalogFeed.Loaded(
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
			channel.send(CatalogFeed.Loaded(
				feed = this,
				throwable = t
			))
		}
	}

	fun getSource(globalId: String): AbstractSource? {
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
	fun SourcesManager.setEnabled(sourceId: String, enable: Boolean) {
		PlatformPreferences["ext_${context.id}_${sourceId}"] = enable
		PlatformPreferences.save()
	}

	/**
	 * Checks if the user has decided to let this extension to be enabled or not.
	 * This function just checks the settings and doesn't check if an extension is loaded or not.
	 * @author MrBoomDev
	 */
	fun SourcesManager.isEnabled(sourceId: String) = PlatformPreferences.getBoolean("ext_${context.id}_${sourceId}") ?: true

	private fun SourcesManager.getAllManagers(): List<SourcesManager> {
		return mutableListOf<SourcesManager>().apply {
			for(abstractSource in getAll()) {
				if(abstractSource is SourcesManager) {
					add(abstractSource)
					addAll(abstractSource.getAllManagers())
				}
			}
		}.toList()
	}

	/**
	 * This method will collect all nested managers from all the places that it could find.
	 */
	fun getAllManagers() = bootstrapManager.getAllManagers()

	private fun SourcesManager.getManager(managerId: String): SourcesManager? {
		for(abstractSource in getAll()) {
			if(abstractSource is SourcesManager) {
				if(abstractSource.context.id == managerId) {
					return abstractSource
				}

				abstractSource.getManager(managerId)?.also {
					return it
				}
			}
		}

		return null
	}

	fun getManager(managerId: String): SourcesManager? {
		return bootstrapManager.getManager(managerId)
	}

	/**
	 * Please note, that this method will only search for
	 * managers loaded directly by [BootstrapManager]
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T: SourcesManager> getManager(clazz: KClass<T>): T? {
		return bootstrapManager.getAll().find {
			it::class == clazz
		} as T?
	}
}