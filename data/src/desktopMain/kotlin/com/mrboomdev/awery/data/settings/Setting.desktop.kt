package com.mrboomdev.awery.data.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

actual fun createSettings(): ObservableSettings {
    return PreferencesSettings(Preferences.userNodeForPackage(AwerySettings::class.java))
}