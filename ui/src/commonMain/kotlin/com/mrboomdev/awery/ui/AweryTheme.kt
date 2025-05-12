package com.mrboomdev.awery.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Awery.TV
import com.mrboomdev.awery.data.settings.Settings

@Composable
expect fun AweryTheme(content: @Composable () -> Unit)

@Composable
fun isDarkTheme(): Boolean {
    // Tv must be always in a dark theme.
    return Awery.TV || when(Settings.darkTheme.state.value) {
        Settings.DarkTheme.AUTO -> isSystemInDarkTheme()
        Settings.DarkTheme.ON -> true
        Settings.DarkTheme.OFF -> false
    }
}