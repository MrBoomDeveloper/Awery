package com.mrboomdev.awery.ext

import java.util.Locale

object AweryClient {
	val NAME: String
		get() = throw NotImplementedError()

	val ID: Int
		get() = throw NotImplementedError()

	val USER_AGENT: String
		get() = throw NotImplementedError()

	val VERSION: Int
		get() = throw NotImplementedError()

	val LOCALE: Locale
		get() = throw NotImplementedError()
}