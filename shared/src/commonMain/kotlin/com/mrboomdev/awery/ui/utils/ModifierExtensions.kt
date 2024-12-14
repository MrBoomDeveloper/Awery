package com.mrboomdev.awery.ui.utils

import androidx.compose.ui.Modifier

/**
 * Returns a new modifier received from the function or itself if nothing was returned.
 */
fun Modifier.update(scope: Modifier.() -> Modifier?) = scope() ?: this