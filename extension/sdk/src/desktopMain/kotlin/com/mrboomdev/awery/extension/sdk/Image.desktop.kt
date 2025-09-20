package com.mrboomdev.awery.extension.sdk

import java.io.File

actual class Image private constructor(
	val url: String?,
	val file: File?,
	val bytes: ByteArray?
) {
	actual constructor(bytes: ByteArray): this(null, null, bytes)
	constructor(url: String): this(url, null, null)
	constructor(file: File): this(null, file, null)
}