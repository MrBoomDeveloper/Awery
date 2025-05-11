package com.mrboomdev.awery.ui.utils

import androidx.annotation.IntDef
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
	val direction = LocalLayoutDirection.current
	
	return PaddingValues(
		start = calculateStartPadding(direction) + paddingValues.calculateStartPadding(direction),
		end = calculateEndPadding(direction) + paddingValues.calculateEndPadding(direction),
		top = calculateTopPadding() + paddingValues.calculateTopPadding(),
		bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding()
	)
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun PaddingValues.only(
	start: Boolean = true, 
	top: Boolean = true, 
	end: Boolean = true, 
	bottom: Boolean = true
): PaddingValues { 
	val direction = LocalLayoutDirection.current
	
	return PaddingValues(
		start = if(start) calculateStartPadding(direction) else 0.dp,
		end = if(end) calculateEndPadding(direction) else 0.dp,
		top = if(top) calculateTopPadding() else 0.dp,
		bottom = if(bottom) calculateBottomPadding() else 0.dp
	) 
}

@IntDef(WINDOW_SIZE_COMPACT, WINDOW_SIZE_MEDIUM, WINDOW_SIZE_EXPANDED)
annotation class WindowSizeClassValue

const val WINDOW_SIZE_COMPACT = 1
const val WINDOW_SIZE_MEDIUM = 2
const val WINDOW_SIZE_EXPANDED = 3

operator fun WindowSizeClass.compareTo(@WindowSizeClassValue windowSizeClass: Int): Int {
	return when(windowWidthSizeClass) {
		WindowWidthSizeClass.COMPACT -> when(windowSizeClass) {
			WINDOW_SIZE_COMPACT -> 0
			else -> -1
		}
		
		WindowWidthSizeClass.MEDIUM -> when(windowSizeClass) {
			WINDOW_SIZE_COMPACT -> 1
			WINDOW_SIZE_MEDIUM -> 0
			else -> -1
		}
		
		WindowWidthSizeClass.EXPANDED -> when(windowSizeClass) {
			WINDOW_SIZE_EXPANDED -> 0
			else -> 1
		}
		
		else -> throw IllegalStateException("Unsupported window size class!")
	}
}

/**
 * Returns an modified modifier from the function or itself if nothing was returned.
 */
inline fun Modifier.update(scope: Modifier.() -> Modifier?) = scope() ?: this

@Suppress("DEPRECATION")
inline fun <reified T> getSerializableNavType(): NavType<T> {
	return object : NavType<T>(
		isNullableAllowed = true
	) {
		override fun get(bundle: Bundle, key: String): T? {
			return bundle[key] as? T
		}
		
		override fun put(bundle: Bundle, key: String, value: T) {
			bundle.putString(key, Json.encodeToString(value))
		}
		
		override fun parseValue(value: String): T {
			return Json.decodeFromString<T>(URLDecoder.decode(value))
		}
		
		override fun serializeAsValue(value: T): String {
			return URLEncoder.encode(Json.encodeToString<T>(value))
		}
	}
}