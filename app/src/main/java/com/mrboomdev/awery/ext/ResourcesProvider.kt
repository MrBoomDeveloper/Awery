package com.mrboomdev.awery.ext

import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.util.Image
import java.io.File

object ResourcesProvider {
	fun getFile(source: Source, path: String): File {
		throw UnsupportedOperationException("Stub1!")
	}

	fun createImage(file: File): Image {
		throw UnsupportedOperationException("Stub1!")
	}
}