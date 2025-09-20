package com.mrboomdev.awery.ui.utils

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateHandle.Companion.validateValue

actual fun <T : Any> SavedStateHandle.saveable(
    key: String,
    saver: Saver<T, out Any>,
    init: () -> T,
): T {
    @Suppress("UNCHECKED_CAST")
    saver as Saver<T, Any>
    // value is restored using the SavedStateHandle or created via [init] lambda
    @Suppress("DEPRECATION") // Bundle.get has been deprecated in API 31
    val value = get<Bundle?>(key)?.get("value")?.let(saver::restore) ?: init()

    // Hook up saving the state to the SavedStateHandle
    setSavedStateProvider(key) {
        bundleOf("value" to with(saver) { SaverScope(::validateValue).save(value) })
    }

    return value
}