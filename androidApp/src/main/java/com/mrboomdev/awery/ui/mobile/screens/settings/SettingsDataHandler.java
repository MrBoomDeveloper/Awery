package com.mrboomdev.awery.ui.mobile.screens.settings;

import com.mrboomdev.awery.data.settings.SettingsItem;

public interface SettingsDataHandler {
	void onScreenLaunchRequest(SettingsItem item);
	void saveValue(SettingsItem item, Object newValue);
	Object restoreValue(SettingsItem item);
}