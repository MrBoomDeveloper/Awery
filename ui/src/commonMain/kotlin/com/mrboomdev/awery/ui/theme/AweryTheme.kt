package com.mrboomdev.awery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.settings.AwerySettings
import kotlinx.serialization.json.JsonNull.content

@Composable
expect fun isMaterialYouAvailable(): Boolean

@Composable
expect fun materialYouColorScheme(
    darkTheme: Boolean = isDarkTheme()
): ColorScheme

@Composable
expect fun AweryTheme(
    darkTheme: Boolean = when {
        Awery.isTv -> true
        else -> isDarkTheme()
    },
    content: @Composable () -> Unit
)

@Composable
expect fun aweryColorScheme(dark: Boolean = isDarkTheme()): ColorScheme

@Composable
fun isDarkTheme(): Boolean {
    // Tv must be always in a dark theme.
    return Awery.isTv || when(AwerySettings.darkTheme.state.value) {
        AwerySettings.DarkTheme.AUTO -> isSystemInDarkTheme()
        AwerySettings.DarkTheme.ON -> true
        AwerySettings.DarkTheme.OFF -> false
    }
}

@Composable
fun isAmoledTheme(): Boolean {
    return isDarkTheme() && AwerySettings.amoledTheme.state.value
}

internal fun ColorScheme.toAmoledColorScheme() = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF060606),
    surfaceContainerHigh = Color(0xFF080808),
    surfaceContainerHighest = Color(0xFF090909),
    surfaceBright = Color.Black,
    surfaceDim = Color.Black,
    surfaceTint = Color.Black,
    surfaceVariant = Color.Black
)