package com.mrboomdev.awery.core.utils

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

fun OutputStream.write(
	inputStream: InputStream,
	buffer: Int = 1024
) {
	val buffer = ByteArray(buffer)
	var length: Int

	while((inputStream.read(buffer).also { length = it }) != -1) {
		write(buffer, 0, length)
	}
}

fun InputStream.readAsString(buffer: Int = 1024): String {
	val result = ByteArrayOutputStream()
	val buffer = ByteArray(buffer)
	var length: Int
	
	while((read(buffer).also { length = it }) != -1) {
		result.write(buffer, 0, length)
	}
	
	return result.toString("UTF-8")
}

fun InputStream.readAsByteArray(buffer: Int = 1024): ByteArray {
	val result = ByteArrayOutputStream()
	val buffer = ByteArray(buffer)
	var length: Int

	while((read(buffer).also { length = it }) != -1) {
		result.write(buffer, 0, length)
	}

	return result.toByteArray()
}