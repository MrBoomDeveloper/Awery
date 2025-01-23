package com.mrboomdev.awery

import com.mrboomdev.awery.data.settings.NicePreferences
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.generated.GeneratedSetting

fun GeneratedSetting.asDeprecatedSetting(): SettingsItem = NicePreferences.getSettingsMap().findItem(key)