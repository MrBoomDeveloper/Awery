package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.source.AbstractSource
import com.mrboomdev.awery.ext.source.Context
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.PendingTask
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.tachiyomi.TachiyomiManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectIndexed
import java.io.InputStream
import android.content.Context as AndroidContext

/**
 * This is an pre-installed extensions manager, which does control ALL extensions.
 * Every manager may have nested managers, so this one is the ROOT of all.
 * @author MrBoomDev
 */
class BootstrapManager(
	private val androidContext: AndroidContext
): SourcesManager(object : Context() {
	override val features = arrayOf<AweryFeature>()
	override val id = ID
	override val isEnabled = true
	override val name = "Bootstrap Sources Manager"
	override val exception = null
	override val icon = null
}) {
	private val managers = mutableMapOf<String, SourcesManager>()

	override fun get(id: String): AbstractSource? = managers[id]
	override fun getAll(): List<AbstractSource> = managers.values.toList()

	override fun onLoad(): PendingTask<Flow<Progress>> {
		YomiManager.initYomiShit(androidContext)
		var totalSize = 0L

		managers[AniyomiManager.ID] = AniyomiManager(androidContext)
		managers[TachiyomiManager.ID] = TachiyomiManager(androidContext)
//		managers[CloudstreamManager.ID] = CloudstreamManager()
//		managers[LnReaderManager.ID] = LnReaderManager()
//		managers[KaguyaManager.ID] = KaguyaManager()
//		managers[MiruManager.ID] = MiruManager()

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

	companion object {
		const val ID = "BOOTSTRAP"
	}
}