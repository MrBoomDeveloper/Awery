package com.mrboomdev.awery.util.extensions

import android.net.Uri
import android.provider.OpenableColumns
import androidx.media3.common.MimeTypes
import com.mrboomdev.awery.platform.Platform
import java.io.File
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

private val REMOVE_LAST_URL_CHARS = arrayOf("/", "?", "#", "&", " ")

fun Uri.toMimeType(): String {
	if(scheme == "content") {
		Platform.contentResolver.query(
			this, null, null, null, null
		)?.use { cursor ->
			return cursor.getString(cursor.getColumnIndexOrThrow(
				OpenableColumns.DISPLAY_NAME
			)).toMimeType(true)
		}
	}

	return (lastPathSegment ?: toString().let {
		it.substring(it.lastIndexOf("/") + 1)
	}).toMimeType(true)
}

fun File.toMimeType(): String {
	return name.toMimeType(true)
}

fun String.toMimeType(): String {
	return toMimeType(false)
}

private fun String.toMimeType(isName: Boolean): String {
	var fileName = if(isName) this
	else File(this).name

	if(fileName.contains("#")) {
		fileName = fileName.substring(0, fileName.indexOf("#"))
	}

	if(fileName.contains("?")) {
		fileName = fileName.substring(0, fileName.indexOf("?"))
	}

	if(fileName.contains("/")) {
		fileName = fileName.substring(0, fileName.indexOf("/"))
	}

	return when(val ext = fileName.substring(fileName.lastIndexOf(".") + 1)) {
		"vtt" -> MimeTypes.TEXT_VTT
		"srt" -> MimeTypes.APPLICATION_SUBRIP
		"scc" -> MimeTypes.APPLICATION_CEA708
		"ts" -> MimeTypes.APPLICATION_DVBSUBS
		"mka" -> MimeTypes.APPLICATION_MATROSKA
		"wvtt" -> MimeTypes.APPLICATION_MP4VTT
		"pgs" -> MimeTypes.APPLICATION_PGS
		"rtsp" -> MimeTypes.APPLICATION_RTSP
		"ass", "ssa" -> MimeTypes.APPLICATION_SS
		"ttml", "xml", "dfxp" -> MimeTypes.APPLICATION_TTML
		"tx3g" -> MimeTypes.APPLICATION_TX3G
		"idx", "sub" -> MimeTypes.APPLICATION_VOBSUB
		else -> throw IllegalArgumentException("Unknown mime type! $ext")
	}
}

fun String.isValidUrl(): Boolean {
	if(isBlank()) {
		return false
	}

	try {
		URL(this).toURI()
		return true
	} catch(e: URISyntaxException) {
		return false
	} catch(e: MalformedURLException) {
		return false
	}
}

fun String.cleanUrl(): String {
	var url = this

	loop@ while(true) {
		for(character in REMOVE_LAST_URL_CHARS) {
			if(url.endsWith(character)) {
				url = url.substring(0, url.length - 1)
				continue@loop
			}
		}

		break
	}

	return url
}

fun String.removeIndent(): String {
	val builder = StringBuilder()
	val iterator = lines().iterator()

	while(iterator.hasNext()) {
		builder.append(iterator.next().trim())

		if(iterator.hasNext()) {
			builder.append("\n")
		}
	}

	return builder.toString()
}