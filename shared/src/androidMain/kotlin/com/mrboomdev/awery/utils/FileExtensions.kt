@file:JvmName("FileExtensionsAndroid")

package com.mrboomdev.awery.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import com.mrboomdev.awery.data.FileType
import com.mrboomdev.awery.platform.Platform

val Uri.fileType: FileType?
	get() = fileName?.let { FileType.test(it) }

fun readClasspath(path: String) = Platform::class.java.classLoader!!
	.getResourceAsStream(path.normalizeFilePath())!!.use { it.readBytes() }

fun readAssets(path: String) = 
	Platform.assets.open(path).readBytes().decodeToString()

@get:SuppressLint("Range")
val Uri.fileName: String?
	get() = with(Platform.contentResolver) {
		query(
			this@fileName,
			arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
			null,
			null,
			null
		)?.use { cursor ->
			cursor.moveToFirst()
			cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
		} ?: tryOrNull { toFile().name }
	}