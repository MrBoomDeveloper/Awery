package com.mrboomdev.awery.utils

import java.io.File

operator fun File.div(child: String) = File(this, child)

val File.totalSize: Long
	get() {
		val children = listFiles() ?: return length()
		var totalSize = 0L
			
		for(child in children) {
			@Suppress("RecursivePropertyAccessor")
			totalSize += child.totalSize
		}
			
		return totalSize
	}



