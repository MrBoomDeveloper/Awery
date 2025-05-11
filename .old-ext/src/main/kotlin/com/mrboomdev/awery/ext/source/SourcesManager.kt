package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.util.PendingTask
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

/**
 * An context will be created by the manager,
 * so that you won't need to repeat same values multiple times.
 */
abstract class SourcesManager: AbstractSource() {
	abstract operator fun get(id: String): AbstractSource?
	abstract fun getAll(): List<AbstractSource>

	@Throws(ExtensionLoadException::class)
	abstract suspend fun load(id: String)
	abstract fun onLoad(): PendingTask<Flow<Progress>>

	/**
	 * User has decided to install an source, so even after restart
	 * it should be loaded again with the same id.
	 */
	@Throws(ExtensionInstallException::class)
	abstract suspend fun install(data: PendingTask<InputStream>): AbstractSource

	/**
	 * An source should be removed from the storage of an device and even after restart
	 * it should remain uninstalled, so that user won't be able to reference
	 * the source until installing it again.
	 */
	abstract suspend fun uninstall(id: String)

	/**
	 * We should to unload an source from the memory.
	 * @param removeFromSource If false, then keep an source just as an stub source,
	 * keeping only it's manifest data like an id, name and so on without external data loading.
	 * All data-loading methods of an source should throw an [UnsupportedOperationException] if called.
	 */
	protected abstract fun unload(id: String, removeFromSource: Boolean)
	fun unload(id: String) = unload(id, false)

	override fun onUnload()  {
		getAll().also { all ->
			for(source in all) {
				unload(source.context.id, true)
			}
		}
	}
}