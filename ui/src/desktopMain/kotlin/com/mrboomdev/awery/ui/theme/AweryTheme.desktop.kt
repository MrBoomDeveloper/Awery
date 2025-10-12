package com.mrboomdev.awery.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState

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
	val primary by AwerySettings.primaryColor.collectAsState()
	val amoled by AwerySettings.amoledTheme.collectAsState()
	
    if(primary >= 0L) {
        return seedColorScheme(seedColor = Color(primary)).let {
            if(amoled || Awery.isTv) it.toAmoledColorScheme() else it
        }
    }
    
    return when(dark) {
        true -> darkColorScheme().let {
            if(amoled) it.toAmoledColorScheme() else it
        }

        else -> expressiveLightColorScheme()
    }
}