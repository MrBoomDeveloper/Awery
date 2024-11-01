package com.mrboomdev.awery.app

import android.content.Context
import com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.tachiyomi.TachiyomiManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

object ExtensionsManager {
	private val managers = mutableListOf<SourcesManager<*>>()

	fun init(context: Context) = flow {
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
			managers.map { manager ->
				async {
					manager.loadAll().onEach {
						maxValues[manager] = it.max
						progress.max = maxValues.values.sum()
						progress.increment()
						emit(progress)
					}
				}
			}.awaitAll()

			progress.isCompleted = true
			emit(progress)
		}
	}

	fun getSource(globalId: String): Source? {
		return globalId.split(";;;").let {
			// Due to compatibility we have to trim all shit after :
			get(it[0])?.get(it[1].split(":")[0])
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

	operator fun get(managerId: String): SourcesManager<*>? {
		return managers.find {
			managerId == it.id
		}
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <T: SourcesManager<*>> get(clazz: KClass<T>): T? {
		return managers.find {
			clazz == it::class
		} as T?
	}
}