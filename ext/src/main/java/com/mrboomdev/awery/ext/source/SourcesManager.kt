package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

abstract class SourcesManager<T : Source> {
	abstract val id: String
	abstract val name: String

	abstract operator fun get(id: String): T?
	abstract fun getAll(): List<T>

	@Throws(ExtensionLoadException::class)
	abstract suspend fun load(id: String)
	abstract fun loadAll(): Flow<Progress>

	@Throws(ExtensionInstallException::class)
	abstract suspend fun install(data: DataSource<InputStream>): T
	abstract suspend fun uninstall(id: String)

	abstract suspend fun unload(id: String)
	abstract fun unloadAll(): Flow<Progress>
}