@file:Suppress("SameParameterValue", "KotlinConstantConditions")

package com.mrboomdev.awery.utils

import java.io.UnsupportedEncodingException
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

fun <T: Enum<T>> String.toEnum(clazz: Class<T>): T? {
	return try {
		java.lang.Enum.valueOf(clazz, this)
	} catch(e: IllegalArgumentException) { null }
}

inline fun <reified T: Enum<T>> String.toEnum(): T? {
	return toEnum(T::class.java)
}

private const val NOT_FOUND = -1
private const val DEFAULT_ENCODING = "UTF-8"
private val HEX_DIGITS = "0123456789ABCDEF".toCharArray()
private const val INVALID_INPUT_CHARACTER = '\ufffd'

/**
 * Encodes characters in the given string as '%'-escaped octets
 * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
 * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
 * all other characters with the exception of those specified in the
 * allow argument.
 *
 * @param allow set of additional characters to allow in the encoded form,
 *  null if no characters should be skipped
 * @return an encoded version of s suitable for use as a URI component
 */
// Stolen from android.net.Uri
fun String.encodeUri(allow: String? = null): String {
	val s = this
	
	// Lazily-initialized buffers.
	var encoded: StringBuilder? = null
	
	val oldLength = s.length
	
	// This loop alternates between copying over allowed characters and
	// encoding in chunks. This results in fewer method calls and
	// allocations than encoding one character at a time.
	var current = 0
	while(current < oldLength) {
		// Start in "copying" mode where we copy over allowed chars.
		
		// Find the next character which needs to be encoded.
		
		var nextToEncode = current
		while(nextToEncode < oldLength && isAllowed(s[nextToEncode], allow)) {
			nextToEncode++
		}
		
		// If there's nothing more to encode...
		if(nextToEncode == oldLength) {
			if(current == 0) {
				// We didn't need to encode anything!
				return s
			} else {
				// Presumably, we've already done some encoding.
				encoded!!.append(s, current, oldLength)
				return encoded.toString()
			}
		}
		
		if(encoded == null) {
			encoded = StringBuilder()
		}
		
		if(nextToEncode > current) {
			// Append allowed characters leading up to this point.
			encoded.append(s, current, nextToEncode)
		} else {
			// assert nextToEncode == current
		}
		
		// Switch to "encoding" mode.
		
		// Find the next allowed character.
		current = nextToEncode
		var nextAllowed = current + 1
		while(nextAllowed < oldLength
			&& !isAllowed(s[nextAllowed], allow)
		) {
			nextAllowed++
		}
		
		// Convert the substring to bytes and encode the bytes as
		// '%'-escaped octets.
		val toEncode = s.substring(current, nextAllowed)
		try {
			val bytes = toEncode.toByteArray(charset(DEFAULT_ENCODING))
			val bytesLength = bytes.size
			for(i in 0 until bytesLength) {
				encoded.append('%')
				encoded.append(HEX_DIGITS[(bytes[i].toInt() and 0xf0) shr 4])
				encoded.append(HEX_DIGITS[bytes[i].toInt() and 0xf])
			}
		} catch(e: UnsupportedEncodingException) {
			throw AssertionError(e)
		}
		
		current = nextAllowed
	}
	
	// Encoded could still be null at this point if s is empty.
	return encoded?.toString() ?: s
}

private fun isAllowed(c: Char, allow: String?): Boolean {
	return (c in 'A'..'Z')
			|| (c in 'a'..'z')
			|| (c in '0'..'9')
			|| "_-!.~'()*".indexOf(c) != NOT_FOUND || (allow != null && allow.indexOf(c) != NOT_FOUND)
}

/**
 * Decodes '%'-escaped octets in the given string using the UTF-8 scheme.
 * Replaces invalid octets with the unicode replacement character
 * ("\\uFFFD").
 *
 * @return the given string with escaped octets decoded
 */
fun String.decodeUri() = decode(
	this, false, StandardCharsets.UTF_8, false
)

@Suppress("SameParameterValue")
private fun decode(
	s: String, convertPlus: Boolean, charset: Charset, throwOnFailure: Boolean
): String {
	val builder = StringBuilder(s.length)
	appendDecoded(builder, s, convertPlus, charset, throwOnFailure)
	return builder.toString()
}

private fun appendDecoded(
	builder: StringBuilder,
	s: String,
	convertPlus: Boolean,
	charset: Charset,
	throwOnFailure: Boolean
) {
	val decoder = charset.newDecoder()
		.onMalformedInput(CodingErrorAction.REPLACE)
		.replaceWith("\ufffd")
		.onUnmappableCharacter(CodingErrorAction.REPORT)
	// Holds the bytes corresponding to the escaped chars being read (empty if the last char
	// wasn't a escaped char).
	val byteBuffer = ByteBuffer.allocate(s.length)
	var i = 0
	while(i < s.length) {
		var c = s[i]
		i++
		when(c) {
			'+' -> {
				flushDecodingByteAccumulator(
					builder, decoder, byteBuffer, throwOnFailure
				)
				builder.append(if(convertPlus) ' ' else '+')
			}
			
			'%' -> {
				// Expect two characters representing a number in hex.
				var hexValue: Byte = 0
				var j = 0
				while(j < 2) {
					try {
						c = getNextCharacter(s, i, s.length, null /* name */)
					} catch(e: URISyntaxException) {
						// Unexpected end of input.
						if(throwOnFailure) {
							throw java.lang.IllegalArgumentException(e)
						} else {
							flushDecodingByteAccumulator(
								builder, decoder, byteBuffer, throwOnFailure
							)
							builder.append(INVALID_INPUT_CHARACTER)
							return
						}
					}
					i++
					val newDigit: Int = hexCharToValue(c)
					if(newDigit < 0) {
						if(throwOnFailure) {
							throw IllegalArgumentException(
								unexpectedCharacterException(s, null,  /* name */c, i - 1)
							)
						} else {
							flushDecodingByteAccumulator(
								builder, decoder, byteBuffer, throwOnFailure
							)
							builder.append(INVALID_INPUT_CHARACTER)
							break
						}
					}
					hexValue = (hexValue * 0x10 + newDigit).toByte()
					j++
				}
				byteBuffer.put(hexValue)
			}
			
			else -> {
				flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure)
				builder.append(c)
			}
		}
	}
	flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure)
}

private fun flushDecodingByteAccumulator(
	builder: java.lang.StringBuilder,
	decoder: CharsetDecoder,
	byteBuffer: ByteBuffer,
	throwOnFailure: Boolean
) {
	if(byteBuffer.position() == 0) {
		return
	}
	byteBuffer.flip()
	try {
		builder.append(decoder.decode(byteBuffer))
	} catch(e: CharacterCodingException) {
		if(throwOnFailure) {
			throw java.lang.IllegalArgumentException(e)
		} else {
			builder.append(INVALID_INPUT_CHARACTER)
		}
	} finally {
		// Use the byte buffer to write again.
		byteBuffer.flip()
		byteBuffer.limit(byteBuffer.capacity())
	}
}

private fun hexCharToValue(c: Char): Int {
	if(c in '0'..'9') {
		return c.code - '0'.code
	}
	if(c in 'a'..'f') {
		return 10 + c.code - 'a'.code
	}
	if(c in 'A'..'F') {
		return 10 + c.code - 'A'.code
	}
	return -1
}

private fun unexpectedCharacterException(
	uri: String, name: String?, unexpected: Char, index: Int
): URISyntaxException {
	val nameString = if((name == null)) "" else " in [$name]"
	return URISyntaxException(
		uri, "Unexpected character$nameString: $unexpected", index
	)
}

@Throws(URISyntaxException::class)
private fun getNextCharacter(uri: String, index: Int, end: Int, name: String?): Char {
	if(index >= end) {
		val nameString = if((name == null)) "" else " in [$name]"
		throw URISyntaxException(
			uri, "Unexpected end of string$nameString", index
		)
	}
	return uri[index]
}