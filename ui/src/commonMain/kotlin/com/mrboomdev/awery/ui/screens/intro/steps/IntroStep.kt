package com.mrboomdev.awery.ui.screens.intro.steps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
sealed interface IntroStep {
    @Composable
    fun Content(singleStep: Boolean, contentPadding: PaddingValues)
}