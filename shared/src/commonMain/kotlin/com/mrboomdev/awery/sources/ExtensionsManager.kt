package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.source.AbstractSource
import com.mrboomdev.awery.ext.source.Context
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.GlobalId
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
	
	fun getSource(globalId: GlobalId): AbstractSource? =
		globalId.sourceId?.let { getManager(globalId.managerId)?.get(it) }
	
	var GlobalId.isEnabled
		get() = PlatformPreferences.getBoolean("ext_${managerId}_${sourceId}") ?: true
		set(value) {
			PlatformPreferences["ext_${managerId}_${sourceId}"] = value
			PlatformPreferences.save()
		}
	
	private fun SourcesManager.getAllRecursive(): List<AbstractSource> = buildList {
		for(abstractSource in getAll()) {
			add(abstractSource)
			
			if(abstractSource is SourcesManager) {
				addAll(abstractSource.getAllRecursive())
			}
		}
	}
	
	/**
	 * All nested [SourcesManager] collected into a single list
	 */
	val allManagers: List<SourcesManager> 
		get() = BootstrapManager.getAllRecursive().filterIsInstance<SourcesManager>()
	
	/**
	 * All nested [Source] collected into a single list
	 */
	val allSources: List<Source> 
		get() = BootstrapManager.getAllRecursive().filterIsInstance<Source>()

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
	internal object BootstrapManager: SourcesManager() {
		private val managers = mutableMapOf<String, SourcesManager>()
		
		init {
			attachContext(object : Context {
				override val id = "BOOTSTRAP"
				override val isEnabled = true
				override val name = "Bootstrap Sources Manager"
				override val exception = null
				override val icon = null
			})
		}
		
		override fun get(id: String): AbstractSource? = managers[id]
		override fun getAll(): List<AbstractSource> = managers.values.toList()
		
		override fun onLoad(): PendingTask<Flow<Progress>> {
			val progresses = mutableMapOf<SourcesManager, Progress>()
			
			for(manager in createPlatformSourceManagers()) {
				managers[manager.context.id] = manager
			}
			
			val pendingTasks = managers.values.map { manager ->
				val task = manager.onLoad()
				task.size?.let { progresses[manager] = Progress(it) }
				manager to task
			}
			
			val progress = Progress(progresses.values.sumOf { it.max })
			
			return object : PendingTask<Flow<Progress>>() {
				override val size = progress.max
				
				override val data: Flow<Progress>
					get() = channelFlow {
						for((manager, task) in pendingTasks) {
							task.data.collectIndexed { index, it ->
								progresses[manager] = it
								
								if(index == 0) {
									progress.max = progresses.values.sumOf { it.max }
								}
								
								progress.value = progresses.values.sumOf { it.value }
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