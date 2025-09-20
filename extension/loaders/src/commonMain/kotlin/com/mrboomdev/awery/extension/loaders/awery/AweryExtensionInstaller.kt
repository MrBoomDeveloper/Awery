package com.mrboomdev.awery.extension.loaders.awery

import com.mrboomdev.awery.core.utils.CacheStorage
import com.mrboomdev.awery.core.utils.deleteRecursively
import com.mrboomdev.awery.core.utils.openInputStream
import com.mrboomdev.awery.core.utils.readAsByteArray
import com.mrboomdev.awery.core.utils.readAsString
import com.mrboomdev.awery.core.utils.toJavaFile
import com.mrboomdev.awery.core.utils.toPlatformFile
import com.mrboomdev.awery.core.utils.use
import com.mrboomdev.awery.core.utils.useTemporaryFile
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.awery.AweryExtensionManifest
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Image
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.source
import io.github.vinceglb.filekit.withScopedAccess
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.io.inputstream.ZipInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.createDirectories

object AweryExtensionInstaller {
	suspend fun install(target: PlatformFile): Extension {
		val id = useTemporaryFile { file ->
			target.copyTo(file)

			val zipFile = ZipFile(file.toJavaFile())

			val manifest = zipFile.getInputStream(
				zipFile.getFileHeader(AweryExtensionConstants.manifestPath)
			).readAsString().let { Json.decodeFromString<AweryExtensionManifest>(it) }

			val outputDirectory = AweryExtensionConstants.installedDirectory.resolve(manifest.id).apply {
				if(exists()) deleteRecursively()
				createDirectories()
			}

			zipFile.extractAll(outputDirectory.absolutePath())
			manifest.id
		}

		return ResolvedExtensionParent(AweryExtensionConstants.installedDirectory.resolve(id)).also { 
			Extensions.add(it)
		}
	}
}