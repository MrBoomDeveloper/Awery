package com.mrboomdev.awery.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi

/**
 * A version of [Font] which doesn't crash on old Android.
 */
@Composable
expect fun FontCompat(
    resource: FontResource,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style)
): Font