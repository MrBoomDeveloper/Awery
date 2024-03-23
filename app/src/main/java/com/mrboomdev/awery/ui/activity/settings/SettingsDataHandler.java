package com.mrboomdev.awery.ui.activity.settings;

import com.mrboomdev.awery.data.settings.SettingsItem;

public interface SettingsDataHandler {
	void onScreenLaunchRequest(SettingsItem item);
	void save(SettingsItem item, Object newValue);
}