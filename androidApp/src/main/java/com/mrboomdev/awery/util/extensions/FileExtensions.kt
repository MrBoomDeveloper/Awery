package com.mrboomdev.awery.util.extensions

import com.mrboomdev.awery.util.io.FileUtil
import java.io.File

fun File.readAssets(): String {
	return FileUtil.readAssets(this)
}