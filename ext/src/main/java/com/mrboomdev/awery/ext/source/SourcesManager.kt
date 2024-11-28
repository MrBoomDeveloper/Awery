package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.util.PendingTask
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.InputStream

abstract class SourcesManager<T : Source> {
	/**
	 * An unique manager id. Only a single manager with the same id may be installed.
	 */
	abstract val id: String

	/**
	 * An display name of the manager which users will see.
	 */
	abstract val name: String

	abstract operator fun get(id: String): T?
	abstract fun getAll(): List<T>

	@Throws(ExtensionLoadException::class)
	abstract suspend fun load(id: String)
	abstract fun loadAll(): PendingTask<Flow<Progress>>

	/**
	 * User has decided to install an source, so even after restart
	 * it should be loaded again with the same id.
	 */
	@Throws(ExtensionInstallException::class)
	abstract suspend fun install(data: PendingTask<InputStream>): T

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
	abstract suspend fun unload(id: String, removeFromSource: Boolean = false)

	/**
	 * This method is being called while unloading an manager,
	 * so all sources should be removed from the memory (not uninstalled!).
	 */
	open fun unloadAll() = channelFlow {
		getAll().also { all ->
			val progress = Progress(all.size.toLong())
			send(progress)

			for(source in all) {
				unload(source.id, true)
				progress.increment()
				send(progress)
			}
		}
	}
}