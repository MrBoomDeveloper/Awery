package com.mrboomdev.awery.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.DynamicColors
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun aweryColorScheme(dark: Boolean): ColorScheme {
	val amoled by AwerySettings.amoledTheme.collectAsState()
	val primary by AwerySettings.primaryColor.collectAsState()
    val context = LocalContext.current
    
    if(primary >= 0L) {
        return seedColorScheme(seedColor = Color(primary)).let {
            if(amoled || Awery.isTv) it.toAmoledColorScheme() else it
        }
    }
    
    return when(dark) {
        true -> (if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else darkColorScheme()).let {
            if(amoled || Awery.isTv) it.toAmoledColorScheme() else it
        }
        
        else -> if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else expressiveLightColorScheme()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun AweryTheme(
    darkTheme: Boolean,
    content: @Composable (() -> Unit)
) {
    MaterialExpressiveTheme(
        typography = AweryTypography,
        colorScheme = aweryColorScheme(darkTheme)
    ) {
        TvMaterialTheme(content)
    }
}

/**
 * Copies colors from the [MaterialTheme] to be used with tv components.
 * Also fixes some defaults so that there will be no boilerplate code.
 */
@Composable
internal fun TvMaterialTheme(content: @Composable () -> Unit) {
    androidx.tv.material3.MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.toTvColorScheme(),
        typography = MaterialTheme.typography.toTvTypography()
    ) {
        content()
    }
}

internal fun Typography.toTvTypography() = androidx.tv.material3.Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall
)

internal fun ColorScheme.toTvColorScheme() = androidx.tv.material3.darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    inversePrimary = inversePrimary,

    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,

    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,

    background = background,
    onBackground = onBackground,

    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceTint = surfaceTint,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,

    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer
)

@Composable
actual fun isMaterialYouAvailable(): Boolean {
	if(Awery.isTv) return false
    return remember { DynamicColors.isDynamicColorAvailable() }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun materialYouColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    
    // User may have restored a backup so we have to fallback to something else
    return if(darkTheme) {
        dynamicDarkColorScheme(context)
    } else dynamicLightColorScheme(context)
}