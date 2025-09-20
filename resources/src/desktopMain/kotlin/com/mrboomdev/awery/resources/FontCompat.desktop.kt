package com.mrboomdev.awery.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

@Composable
actual fun FontCompat(
    resource: FontResource,
    weight: FontWeight,
    style: FontStyle,
    variationSettings: FontVariation.Settings
) = Font(resource, weight, style, variationSettings)