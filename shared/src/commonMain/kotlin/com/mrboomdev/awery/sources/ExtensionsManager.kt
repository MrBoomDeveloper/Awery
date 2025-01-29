package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.source.AbstractSource
import com.mrboomdev.awery.ext.source.Context
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.PendingTask
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.platform.PlatformPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectIndexed
import java.io.InputStream
import kotlin.reflect.KClass

object ExtensionsManager {
	fun init() = BootstrapManager.onLoad()

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
		PlatformPreferences["ext_${id}_${sourceId}"] = enable
		PlatformPreferences.save()
	}

	/**
	 * Checks if the user has decided to let this extension to be enabled or not.
	 * This function just checks the settings and doesn't check if an extension is loaded or not.
	 * @author MrBoomDev
	 */
	fun SourcesManager.isEnabled(sourceId: String) = PlatformPreferences.getBoolean("ext_${id}_${sourceId}") ?: true

	private fun SourcesManager.getAllManagers(): List<SourcesManager> = buildList {
		for(abstractSource in getAll()) {
			if(abstractSource is SourcesManager) {
				add(abstractSource)
				addAll(abstractSource.getAllManagers())
			}
		}
	}

	/**
	 * This method will collect all nested managers from all the places that it could find.
	 */
	fun getAllManagers() = BootstrapManager.getAllManagers()

	private fun SourcesManager.getManager(managerId: String): SourcesManager? {
		for(abstractSource in getAll()) {
			if(abstractSource is SourcesManager) {
				if(abstractSource.id == managerId) {
					return abstractSource
				}

				abstractSource.getManager(managerId)?.also {
					return it
				}
			}
		}

		return null
	}

	fun getManager(managerId: String) = BootstrapManager.getManager(managerId)

	/**
	 * Please note, that this method will only search for
	 * managers loaded directly by [BootstrapManager]
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T: SourcesManager> getManager(clazz: KClass<T>): T? {
		return BootstrapManager.getAll().find {
			it::class == clazz
		} as T?
	}
	
	/**
	 * This is an pre-installed extensions manager, which does control ALL extensions.
	 * Every manager may have nested managers, so this one is the ROOT of all.
	 * @author MrBoomDev
	 */
	internal object BootstrapManager: SourcesManager(object : Context {
		override val features = arrayOf<AweryFeature>()
		override val id = "BOOTSTRAP"
		override val isEnabled = true
		override val name = "Bootstrap Sources Manager"
		override val exception = null
		override val icon = null
	}) {
		private val managers = mutableMapOf<String, SourcesManager>()
		
		override fun get(id: String): AbstractSource? = managers[id]
		override fun getAll(): List<AbstractSource> = managers.values.toList()
		
		override fun onLoad(): PendingTask<Flow<Progress>> {
			var totalSize = 0L
			
			for(manager in createPlatformSourceManagers()) {
				managers[manager.id] = manager
			}
			
			val pendingTasks = managers.values.map { manager ->
				val task = manager.onLoad()
				task.size?.let { totalSize += it }
				manager to task
			}
			
			return object : PendingTask<Flow<Progress>>() {
				override val size = totalSize
				
				override val data: Flow<Progress>
					get() = channelFlow {
						val progresses = mutableMapOf<SourcesManager, Long>()
						val progress = Progress(totalSize)
						
						for((manager, task) in pendingTasks) {
							task.data.collectIndexed { index, it ->
								if(index == 0) {
									progress.max += it.max
								}
								
								progresses[manager] = it.value
								progress.value = progresses.values.sum()
								send(progress)
							}
						}
						
						if(!progress.isCompleted) {
							progress.finish()
							send(progress)
						}
					}
			}
		}
		
		override fun onUnload() {
			for(manager in managers.values) {
				manager.onUnload()
			}
		}
		
		// These methods aren't supposed to be invoked!
		override suspend fun install(data: PendingTask<InputStream>) = throw UnsupportedOperationException()
		override suspend fun uninstall(id: String) = throw UnsupportedOperationException()
		override suspend fun load(id: String) = throw UnsupportedOperationException()
		override fun unload(id: String, removeFromSource: Boolean) = throw UnsupportedOperationException()
	}
}

internal expect fun ExtensionsManager.BootstrapManager.createPlatformSourceManagers(): List<SourcesManager>