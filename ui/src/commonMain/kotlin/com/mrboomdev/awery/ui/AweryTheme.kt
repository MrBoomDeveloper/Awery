package com.mrboomdev.awery.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.data.settings.Settings

@Composable
fun AweryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = platformColorScheme(),
        content = content
    )
}

@Composable
fun isDarkTheme(): Boolean {
    return when(Settings.darkTheme.state.value) {
        Settings.DarkTheme.AUTO -> isSystemInDarkTheme()
        Settings.DarkTheme.ON -> true
        Settings.DarkTheme.OFF -> false
    }
}

@Composable
internal expect fun platformColorScheme(): ColorScheme