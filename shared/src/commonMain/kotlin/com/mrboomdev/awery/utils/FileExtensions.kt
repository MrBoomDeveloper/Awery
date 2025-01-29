package com.mrboomdev.awery.utils

import java.io.File

fun String.normalizeFilePath(): String {
	val n = length
	val normalized = toCharArray()
	var index = 0
	var prevChar = 0.toChar()
	for(i in 0 until n) {
		val current = normalized[i]
		// Remove duplicate slashes.
		if(!(current == '/' && prevChar == '/')) {
			normalized[index++] = current
		}
		
		prevChar = current
	}
	
	// Omit the trailing slash, except when pathname == "/".
	if(prevChar == '/' && n > 1) {
		index--
	}
	
	return if((index != n)) String(normalized, 0, index) else this
}

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



