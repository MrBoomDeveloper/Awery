package com.mrboomdev.awery.app.data.settings.base;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.ext.data.Setting;

@Deprecated(forRemoval = true)
public interface SettingsDataHandler {
	void onScreenLaunchRequest(Setting item);
	void saveValue(Setting item, Object newValue);
	Object restoreValue(Setting item);

	default void restoreSavedValues(@NonNull Setting setting) {
		setting.setValue(restoreValue(setting));

		var items = setting.getItems();

		if(items != null) {
			for(var item : items) {
				if(item == null) continue;
				restoreSavedValues(item);
			}
		}
	}
}