package com.mrboomdev.awery.ui.activity.settings;

import com.mrboomdev.awery.app.data.settings.SettingsItem;

public interface SettingsDataHandler {
	void onScreenLaunchRequest(SettingsItem item);
	void saveValue(SettingsItem item, Object newValue);
	Object restoreValue(SettingsItem item);
}