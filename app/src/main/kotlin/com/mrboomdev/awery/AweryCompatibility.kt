package com.mrboomdev.awery

import com.mrboomdev.awery.data.settings.NicePreferences
import com.mrboomdev.awery.data.settings.SettingsItem

fun GeneratedSetting.asSetting(): SettingsItem = NicePreferences.getSettingsMap().findItem(key)
fun AwerySettings.ThemeColorPaletteValue.findSetting(): SettingsItem = NicePreferences.getSettingsMap().findItem(name)