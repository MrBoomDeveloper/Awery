package com.mrboomdev.awery.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun AweryTheme(content: @Composable (() -> Unit)) {
    MaterialTheme(
        colorScheme = if(isDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}