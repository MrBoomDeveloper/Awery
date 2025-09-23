package com.mrboomdev.awery.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.AwerySettings.darkTheme

@Composable
actual fun isMaterialYouAvailable(): Boolean {
    return false
}

@Composable
actual fun materialYouColorScheme(darkTheme: Boolean): ColorScheme {
    // User may have restored a backup so we have to fallback to something else
    return if(darkTheme) darkColorScheme() else lightColorScheme()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun AweryTheme(
    darkTheme: Boolean,
    content: @Composable (() -> Unit)
) {
    MaterialExpressiveTheme(
        typography = AweryTypography,
        colorScheme = aweryColorScheme(darkTheme),
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun aweryColorScheme(dark: Boolean): ColorScheme {
    if(AwerySettings.primaryColor.state.longValue >= 0L) {
        return seedColorScheme(
            seedColor = Color(AwerySettings.primaryColor.state.longValue)
        ).let {
            if(AwerySettings.amoledTheme.state.value || Awery.isTv) {
                it.toAmoledColorScheme()
            } else it
        }
    }
    
    return when(dark) {
        true -> darkColorScheme().let {
            if(AwerySettings.amoledTheme.state.value) {
                it.toAmoledColorScheme()
            } else it
        }

        else -> expressiveLightColorScheme()
    }
}