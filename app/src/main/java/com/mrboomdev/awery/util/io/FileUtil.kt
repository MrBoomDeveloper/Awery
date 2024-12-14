package com.mrboomdev.awery.util.io

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object FileUtil {

	@get:SuppressLint("Range")
	val Uri.fileName: String?
		get() {
			val resolver = anyContext.contentResolver

			resolver.query(
				this,
				arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
				null,
				null,
				null
			).use { cursor ->
				if(cursor == null) return null

				cursor.moveToFirst()
				return cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
			}
		}

	@JvmStatic
	fun deleteFile(file: File?) {
		if(file == null) return

		if(file.isDirectory) {
			val children = file.listFiles() ?: return

			for(child in children) {
				deleteFile(child)
			}
		}

		file.delete()
	}

	@JvmStatic
	fun getFileSize(file: File): Long {
		if(file.isDirectory) {
			val children = file.listFiles() ?: return 0

			var totalSize: Long = 0

			for(child in children) {
				totalSize += getFileSize(child)
			}

			return totalSize
		}

		return file.length()
	}

	@JvmStatic
	@Throws(IOException::class)
	fun readAssets(file: File): String {
		return readAssets(file.absolutePath.substring(1))
	}

	@JvmStatic
	@Throws(IOException::class)
	fun readAssets(path: String) = appContext.assets.open(path).readBytes().decodeToString()
}