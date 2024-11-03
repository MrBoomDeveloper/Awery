package com.mrboomdev.awery.desktop.sources.yomi

import com.mrboomdev.awery.ext.source.DataSource
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Progress
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

abstract class YomiManager<T: YomiSource>: SourcesManager<T>() {

	override fun get(id: String): T? {
		TODO("Not yet implemented")
	}

	override fun getAll(): List<T> {
		TODO("Not yet implemented")
	}

	override suspend fun load(id: String) {
		TODO("Not yet implemented")
	}

	override fun loadAll(): Flow<Progress> {
		TODO("Not yet implemented")
	}

	override suspend fun install(data: DataSource<InputStream>): T {
		TODO("Not yet implemented")
	}

	override suspend fun uninstall(id: String) {
		TODO("Not yet implemented")
	}

	override suspend fun unload(id: String) {
		TODO("Not yet implemented")
	}

	override fun unloadAll(): Flow<Progress> {
		TODO("Not yet implemented")
	}
}