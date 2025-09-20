package com.mrboomdev.awery.core.utils

import androidx.core.net.toFile
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile
import java.io.File
import java.io.InputStream

actual fun PlatformFile.toJavaFile(): File {
	return when(val file = androidFile) {
		is AndroidFile.FileWrapper -> file.file
		is AndroidFile.UriWrapper -> file.uri.toFile()
	}
}

actual fun PlatformFile.openInputStream(): InputStream {
	return when(val file = androidFile) {
		is AndroidFile.FileWrapper -> file.file.inputStream()
		is AndroidFile.UriWrapper -> Awery.context.contentResolver.openInputStream(file.uri)!!
	}
}