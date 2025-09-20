package com.mrboomdev.awery.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun SeedAweryTheme(
    seedColor: Color,
    content: @Composable (() -> Unit)
) {
    SeedAweryThemeImpl(seedColor) {
        TvMaterialTheme(content)
    }
}