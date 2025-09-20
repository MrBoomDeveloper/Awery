package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.http
import com.mrboomdev.awery.core.utils.download
import com.mrboomdev.awery.core.utils.toJavaFile
import com.mrboomdev.awery.extension.loaders.awery.AweryExtensionInstaller
import com.mrboomdev.awery.extension.sdk.Extension
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.toKotlinxIoPath
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.read
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.io.encoding.Base64

object ExtensionInstaller {
	private val installing = mutableListOf<String>()
	private val installingObservable = MutableStateFlow(emptyList<String>())
	
	fun observeInstalling() = installingObservable.asStateFlow()
	
	suspend fun install(file: PlatformFile): Extension {
		if(file.absolutePath() in installing) {
			throw IllegalArgumentException("${file.absolutePath()} is already installing!")
		}
		
		try {
			installing += file.absolutePath()
			return AweryExtensionInstaller.install(file)
		} finally {
			installing -= file.absolutePath()
			installingObservable.emit(installing.toList())
		}
	}
	
	suspend fun install(managerId: String, url: String) {
		if(url in installing) {
			throw IllegalArgumentException("$url is already installing!")
		}
		
		try {
			installing += url
			installingObservable.emit(installing.toList())

			val encodedUrl = Base64.encode(url.encodeToByteArray())

			val targetFile = FileKit.cacheDir.resolve("download/$encodedUrl").apply {
				parent()?.createDirectories()
				if(exists()) delete()
				toJavaFile().deleteOnExit()
			}

			Awery.http.prepareGet(url).download(targetFile)
			
			installImpl(managerId, targetFile).also { installed ->
				Extensions.add(installed)
			}
			
			targetFile.delete()
		} finally {
			installing -= url
			installingObservable.emit(installing.toList())
		}
	}
}

sealed class ExtensionInstallException(
	message: String? = null,
	cause: Throwable? = null
): Exception(message, cause) {
	class Conflict: ExtensionInstallException()
	class Invalid(message: String?): ExtensionInstallException(message)
	class Unsupported(message: String?): ExtensionInstallException(message)
	class Unknown(message: String?): ExtensionInstallException(message)
	
	class Blocked(
		val blockedBy: String, 
		message: String? = null
	): ExtensionInstallException(message ?: "Install blocked by $blockedBy")
}

internal expect suspend fun installImpl(managerId: String, file: PlatformFile): Extension