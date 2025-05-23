package com.mrboomdev.awery.data.settings

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun platformSettings(): ObservableSettings {
    return SharedPreferencesSettings(
        delegate = Awery.context.getSharedPreferences("Awery", 0),
        commit = true
    )
}