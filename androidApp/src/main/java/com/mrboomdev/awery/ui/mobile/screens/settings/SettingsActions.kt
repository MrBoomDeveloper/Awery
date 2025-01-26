package com.mrboomdev.awery.ui.mobile.screens.settings

import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.platform.PlatformSettingHandler

@Deprecated(message = "old shit")
object SettingsActions {

	@JvmStatic
	fun run(item: SettingsItem) {
		PlatformSettingHandler.handlePlatformClick(anyContext, PlatformSetting(
			key = item.key
		)
		)
	}
}