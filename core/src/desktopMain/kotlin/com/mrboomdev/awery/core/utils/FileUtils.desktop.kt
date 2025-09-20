package com.mrboomdev.awery.core.utils

import io.github.vinceglb.filekit.PlatformFile
import java.io.File
import java.io.InputStream

actual fun PlatformFile.toJavaFile(): File {
	return this.file
}

actual fun PlatformFile.openInputStream(): InputStream {
	return file.inputStream()
}