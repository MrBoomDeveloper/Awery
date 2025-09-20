package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateHandle.Companion.validateValue
import androidx.savedstate.savedState

actual fun <T : Any> SavedStateHandle.saveable(
    key: String,
    saver: Saver<T, out Any>,
    init: () -> T,
): T {
    @Suppress("UNCHECKED_CAST")
    saver as Saver<T, Any>
    // value is restored using the SavedStateHandle or created via [init] lambda
    val value = get<Map<String, Any?>?>(key)?.get("value")?.let(saver::restore) ?: init()

    // Hook up saving the state to the SavedStateHandle
    setSavedStateProvider(key) {
        savedState(mapOf("value" to with(saver) { SaverScope(::validateValue).save(value) }))
    }

    return value
}