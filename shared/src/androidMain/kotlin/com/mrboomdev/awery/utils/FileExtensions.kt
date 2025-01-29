@file:JvmName("FileExtensionsAndroid")

package com.mrboomdev.awery.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import com.mrboomdev.awery.data.FileType
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.platform.android.AndroidGlobals

val Uri.fileType: FileType?
	get() = fileName?.let { FileType.test(it) }

fun readClasspath(path: String) = Platform::class.java.classLoader
	.getResourceAsStream(path.normalizeFilePath())!!.use { it.readBytes() }

fun readAssets(path: String) = 
	AndroidGlobals.applicationContext.assets.open(path).readBytes().decodeToString()

@get:SuppressLint("Range")
val Uri.fileName: String?
	get() = with(AndroidGlobals.applicationContext.contentResolver) {
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