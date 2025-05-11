package com.mrboomdev.awery.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun platformColorScheme(): ColorScheme {
    return when(isDarkTheme()) {
        true -> darkColorScheme()
        false -> lightColorScheme()
    }
}