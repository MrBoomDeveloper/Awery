package com.mrboomdev.awery.ext

import com.mrboomdev.awery.ext.source.Context
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.util.Image
import java.io.File

object ResourcesProvider {
	fun getFile(context: Context, path: String): File = throw NotImplementedError("Stub!")
}