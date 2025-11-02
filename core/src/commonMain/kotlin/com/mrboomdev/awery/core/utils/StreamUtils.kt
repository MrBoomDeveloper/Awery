package com.mrboomdev.awery.core.utils

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Copies the contents of the input stream to this output stream.
 * The input stream is read in chunks of [buffer] bytes and written to this output stream.
 * If no buffer size is specified, it defaults to 1024 bytes.
 */
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

/**
 * Reads the entire input stream as a string using the specified buffer size.
 * The input stream is read in chunks of [buffer] bytes and written to a ByteArrayOutputStream.
 * The contents of the ByteArrayOutputStream are then converted to a string using the UTF-8 charset.
 * If no buffer size is specified, it defaults to 1024 bytes.
 *
 * @param buffer The size of the buffer to use when reading the input stream.
 * @return The contents of the input stream as a string.
 */
fun InputStream.readAsString(buffer: Int = 1024): String {
	val result = ByteArrayOutputStream()
	val buffer = ByteArray(buffer)
	var length: Int
	
	while((read(buffer).also { length = it }) != -1) {
		result.write(buffer, 0, length)
	}
	
	return result.toString("UTF-8")
}

/**
 * Reads the entire input stream as a byte array using the specified buffer size.
 * The input stream is read in chunks of [buffer] bytes and written to a ByteArrayOutputStream.
 * The contents of the ByteArrayOutputStream are then converted to a byte array.
 * If no buffer size is specified, it defaults to 1024 bytes.
 *
 * @param buffer The size of the buffer to use when reading the input stream.
 * @return The contents of the input stream as a byte array.
 */
fun InputStream.readAsByteArray(buffer: Int = 1024): ByteArray {
	val result = ByteArrayOutputStream()
	val buffer = ByteArray(buffer)
	var length: Int

	while((read(buffer).also { length = it }) != -1) {
		result.write(buffer, 0, length)
	}

	return result.toByteArray()
}