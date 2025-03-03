package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import androidx.window.core.layout.WindowHeightSizeClass
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

@Suppress("NOTHING_TO_INLINE")
inline fun WindowSizeClass.isWidthAtLeast(clazz: WindowWidthSizeClass) = windowWidthSizeClass.isAtLeast(clazz)

@Suppress("NOTHING_TO_INLINE")
inline fun WindowSizeClass.isHeightAtLeast(clazz: WindowHeightSizeClass) = windowHeightSizeClass.isAtLeast(clazz)

fun WindowWidthSizeClass.isAtLeast(clazz: WindowWidthSizeClass) = when(clazz) {
	WindowWidthSizeClass.COMPACT -> true
	
	WindowWidthSizeClass.MEDIUM -> this == WindowWidthSizeClass.MEDIUM 
			|| this == WindowWidthSizeClass.EXPANDED
	
	WindowWidthSizeClass.EXPANDED -> this == WindowWidthSizeClass.EXPANDED
	
	// Undefined behaviour
	else -> false
}

fun WindowHeightSizeClass.isAtLeast(clazz: WindowHeightSizeClass) = when(clazz) {
	WindowHeightSizeClass.COMPACT -> true
	
	WindowHeightSizeClass.MEDIUM -> this == WindowHeightSizeClass.MEDIUM
			|| this == WindowHeightSizeClass.EXPANDED
	
	WindowHeightSizeClass.EXPANDED -> this == WindowHeightSizeClass.EXPANDED
	
	// Undefined behaviour
	else -> false
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