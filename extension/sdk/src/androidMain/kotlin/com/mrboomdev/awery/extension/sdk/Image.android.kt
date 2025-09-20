package com.mrboomdev.awery.extension.sdk

import android.graphics.drawable.Drawable
import java.io.File

actual class Image private constructor(
    val drawable: Drawable?,
    val url: String?,
    val file: File?,
    val bytes: ByteArray?
) {
    actual constructor(bytes: ByteArray): this(null, null, null, bytes)
    constructor(drawable: Drawable): this(drawable, null, null, null)
    constructor(url: String): this(null, url, null, null)
    constructor(file: File): this(null, null, file, null)
}