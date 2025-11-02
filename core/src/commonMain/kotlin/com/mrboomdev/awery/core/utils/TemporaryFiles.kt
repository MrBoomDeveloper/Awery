package com.mrboomdev.awery.core.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.resolve
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal val tempDir by lazy {
	FileKit.cacheDir.resolve("temp").apply { 
		runBlocking {
			deleteRecursively()
		}
	}.toJavaFile().apply { 
		mkdirs()
	}
}

/**
 * Creates a temporary file which will be deleted right after [block] execution.
 *
 * The file is created in the cache directory of the application and will be deleted
 * automatically when the application exits, regardless of whether [block] throws an exception
 * or not.
 *
 * @param block A lambda function that takes a [PlatformFile] as an argument and returns a result of type [T].
 * The function will be called with the temporary file as its argument and will be executed exactly once.
 * @return The result of executing [block] with the temporary file as its argument.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> useTemporaryFile(block: (PlatformFile) -> T): T {
	contract { 
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	
	val file = File.createTempFile("temp", null, tempDir)
	file.deleteOnExit()
	
	return try {
		block(file.toPlatformFile())
	} catch(t: Throwable) {
		throw t
	} finally {
		if(file.exists()) {
			file.delete()
		}
	}
}