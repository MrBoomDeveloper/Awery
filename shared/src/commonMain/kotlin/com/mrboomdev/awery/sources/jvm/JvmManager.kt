package com.mrboomdev.awery.sources.jvm

import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.source.AbstractSource
import com.mrboomdev.awery.ext.source.Context
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Image
import com.mrboomdev.awery.ext.util.PendingTask
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.NoSuchFileException

open class JvmManager(
	private val rootDirectory: File,
	context: Context? = null
): SourcesManager(context ?: object : Context() {
	override val features = arrayOf(AweryFeature.INSTALL_STORAGE)
	override val id = ID
	override val isEnabled = true
	override val name = "Awery Extensions"
	override val exception = null
	override val icon = null //TODO: Create an icon
}) {
	private val sources = mutableMapOf<String, AbstractSource>()

	init {
		rootDirectory.mkdirs()
	}

	override fun get(id: String) = sources[id]
	override fun getAll() = sources.values.toList()

	override suspend fun load(id: String) {
		val dir = File(rootDirectory, id)
		var isEnabled = true
		var exception: Throwable? = null
		var entry: JvmEntry? = null
		var main: AbstractSource? = null

		try {
			val classLoader = createClassLoader(File(dir, INSTALLED_FILE))

			entry = (classLoader.getResourceAsStream(ENTRY_FILE) ?: run {
				throw ExtensionLoadException("Extension entry file is missing!")
			}).use {
				it.readBytes().decodeToString()
			}.let { Json.decodeFromString<JvmEntry>(it) }

			val mainClass = classLoader.loadClass(entry.main) ?: run {
				throw ExtensionLoadException("Missing main class!")
			}

			val context = when(entry.type) {
				JvmEntry.Type.MANAGER -> object : Context() {
					override val id = entry.id
					override val name = entry.name

					override val isEnabled: Boolean
						get() = isEnabled

					override val exception: Throwable?
						get() = exception

					override val icon: Image?
						get() = null //TODO: Load an icon

					override val features = entry.features.mapNotNull {
						runCatching { AweryFeature.valueOf(it) }.getOrNull()
					}.toTypedArray()
				}

				JvmEntry.Type.SOURCE -> object : Context.SourceContext() {
					override val id = entry.id
					override val name = entry.name
					override val manager = this@JvmManager

					override val isEnabled: Boolean
						get() = isEnabled

					override val exception: Throwable?
						get() = exception

					override val icon: Image?
						get() = null //TODO: Load an icon

					override val ageRating = entry.ageRating?.runCatching {
						AweryAgeRating.valueOf(this)
					}?.getOrNull()

					override val features = entry.features.mapNotNull {
						runCatching { AweryFeature.valueOf(it) }.getOrNull()
					}.toTypedArray()
				}
			}

			val constructor = try {
				mainClass.getConstructor(context::class.java).apply {
					isAccessible = true
				}
			} catch(e: NoSuchMethodException) {
				throw ExtensionLoadException("No constructor with an context was found!")
			}

			main = constructor.newInstance(context) as AbstractSource
		} catch(e: Throwable) {
			exception = e
			isEnabled = false
		}

		if(main == null) {
			main = object : AbstractSource() {
				override val context = object : Context() {
					override val id = id
					override val name = entry?.name

					override val isEnabled: Boolean
						get() = isEnabled

					override val exception: Throwable?
						get() = exception

					override val icon: Image?
						get() = null //TODO: Load an icon

					override val features = entry?.features?.mapNotNull {
						runCatching { AweryFeature.valueOf(it) }.getOrNull()
					}?.toTypedArray() ?: arrayOf()
				}
			}
		}

		sources[id] = main
	}

	@Suppress("DEPRECATION")
	protected open fun createClassLoader(jar: File): ClassLoader {
		return URLClassLoader(arrayOf(URL(jar.absolutePath)))
	}

	override fun onLoad(): PendingTask<Flow<Progress>> {
		val list = rootDirectory.list() ?: return object : PendingTask<Flow<Progress>>() {
			override val size = 0L

			override val data: Flow<Progress>
				get() = flowOf(Progress())
		}

		return object : PendingTask<Flow<Progress>>() {
			override val size = list.size.toLong()

			override val data: Flow<Progress>
				get() = channelFlow {
					val progress = Progress(list.size.toLong())

					for(source in list) {
						load(source)
						progress.increment()
						send(progress)
					}

					if(!progress.isCompleted) {
						progress.finish()
						send(progress)
					}
				}
		}
	}

	override suspend fun install(data: PendingTask<InputStream>): AbstractSource {
		TODO("Not yet implemented")
	}

	override suspend fun uninstall(id: String) {
		if(id !in sources) return
		unload(id, true)
		File(rootDirectory, id).deleteRecursively()
	}

	override fun unload(id: String, removeFromSource: Boolean) {
		val source = sources[id] ?: return
		source.onUnload()

		if(removeFromSource) {
			sources.remove(id)
		}
	}

	companion object {
		const val ID = "JvmManager"
		const val ENTRY_FILE = "awery_extension_entry.json"
		const val INSTALLED_FILE = "installed.jar"
		const val INSTALLED_DATA_DIR = "data"
	}
}