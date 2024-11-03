package com.mrboomdev.awery.util

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Serializable
import java.net.URLDecoder
import java.net.URLEncoder

object NavUtils {

	@Suppress("DEPRECATION")
	inline fun <reified T : Serializable> getSerializableNavType(): NavType<T> {
		return object : NavType<T>(
			isNullableAllowed = true
		) {
			override fun get(bundle: Bundle, key: String): T? {
				return bundle.getSerializable(key) as? T
			}

			override fun put(bundle: Bundle, key: String, value: T) {
				bundle.putSerializable(key, value)
			}

			override fun parseValue(value: String): T {
				return Json.decodeFromString<T>(URLDecoder.decode(value))
			}

			override fun serializeAsValue(value: T): String {
				return URLEncoder.encode(Json.encodeToString<T>(value))
			}
		}
	}
}