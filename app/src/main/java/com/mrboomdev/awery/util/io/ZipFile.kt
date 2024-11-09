package com.mrboomdev.awery.util.io

import android.net.Uri
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val BUFFER_SIZE = 1024 * 5

fun zipFiles(paths: Map<String, File>, into: OutputStream) {
	ZipOutputStream(into).use { out ->
		val data = ByteArray(BUFFER_SIZE)
		for((key, file) in paths) {
			BufferedInputStream(FileInputStream(file), BUFFER_SIZE).use { stream ->
				val zipEntry = ZipEntry(key)
				out.putNextEntry(zipEntry)
				var read: Int

				while((stream.read(data).also { read = it }) != -1) {
					out.write(data, 0, read)
				}

				out.closeEntry()
			}
		}
	}
}

fun zipFiles(paths: Map<String, File>, into: Uri) {
	appContext.contentResolver.openOutputStream(into).use { out ->
		zipFiles(paths, out!!)
	}
}

fun unzipFiles(input: Uri, output: File) {
	appContext.contentResolver.openInputStream(input).use { stream ->
		unzipFiles(stream!!, output)
	}
}

fun unzipFiles(input: InputStream, output: File) {
	output.mkdirs()

	ZipInputStream(BufferedInputStream(input, BUFFER_SIZE)).use { zin ->
		var ze: ZipEntry
		while((zin.nextEntry.also { ze = it }) != null) {
			val path = File(output, ze.name)

			if(ze.isDirectory && !path.isDirectory) {
				path.mkdirs()
			} else {
				val parent = path.parentFile
				parent?.mkdirs()

				BufferedOutputStream(FileOutputStream(path, false), BUFFER_SIZE).use { stream ->
					var read = zin.read()
					while(read != -1) {
						stream.write(read)
						read = zin.read()
					}
				}
			}
		}
	}
}