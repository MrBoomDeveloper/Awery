package com.mrboomdev.awery.extension.loaders.awery

import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.deleteRecursively
import com.mrboomdev.awery.core.utils.toJavaFile
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.awery.AweryExtensionManifest
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.sdk.Image
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.modules.ManageableModule
import com.mrboomdev.awery.extension.sdk.modules.ManagerModule
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.resolve
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import net.lingala.zip4j.ZipFile
import java.nio.file.Files.exists
import kotlin.io.encoding.Base64

class ResolvedExtensionParent(
	private val directory: PlatformFile
): Extension {
	private val manifest: AweryExtensionManifest
	private var children: Extension? = null
	
	override val icon by lazy { 
		if(manifest.icon == null) {
			return@lazy null
		}
		
		try {
			runBlocking {
				Image(directory.resolve(manifest.icon).readBytes())
			}
		} catch(_: Throwable) {
			Log.e("ResolvedExtensionParent", "Failed to load an icon from " +
					"${directory.resolve(manifest.icon)}")
			
			null
		}
	}
	
	private var _loadException: ExtensionLoadException? = null
	override val loadException get() = _loadException

	init {
		manifest = try {
			runBlocking {
				directory.resolve(AweryExtensionConstants.manifestPath).readString().let {
					AweryExtensionConstants.manifestJsonFormat.decodeFromString(it)
				}
			}
		} catch(t: Throwable) {
			_loadException = ExtensionLoadException(cause = t)
			
			AweryExtensionManifest(
				main = "Cannot parse manifest.main field!",
				name = directory.name,
				id = directory.name,
				nsfw = false
			)
		}
	}
	
	override val name = manifest.name
	override val id = "${manifest.id}_parent"
	override val version = manifest.version
	override val isNsfw = manifest.nsfw
	override val webpage = manifest.webpage
	override val lang = manifest.lang
	
	private fun loadChildren() {
		children = try {
			loadMain(object : Extension by this {
				override val id = manifest.id
			}, directory.resolve(AweryExtensionConstants.platformBinary).apply { 
				if(!exists()) {
					throw ExtensionLoadException.IllegalArchitecture(
						"Platform binary was not found at ${directory.resolve(
							AweryExtensionConstants.platformBinary)}!")
				}
				
				toJavaFile().setWritable(false)
			}, manifest.main)
		} catch(t: Throwable) {
			_loadException = ExtensionLoadException(cause = t)
			null
		}
	}

	override fun createModules() = listOf(
		object : ManagerModule {
			override fun getAll() = flow {
				if(children == null && loadException == null) {
					loadChildren()
				}
				
				if(children != null) {
					emit(children!!)
				}
			}
		},
		
		object : ManageableModule {
			override fun getPreferences(): List<Preference<*>> = emptyList()
			override fun onSavePreferences(preferences: List<Preference<*>>) {}

			override suspend fun uninstall() {
				AweryExtensionConstants.installedDirectory.resolve(manifest.id).deleteRecursively()
				Extensions.remove(this@ResolvedExtensionParent)
			}
		}
	)
}

internal expect fun loadMain(
	parent: Extension,
	binary: PlatformFile, 
	main: String
): Extension