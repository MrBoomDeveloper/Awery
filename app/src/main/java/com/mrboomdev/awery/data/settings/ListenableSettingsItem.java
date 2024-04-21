package com.mrboomdev.awery.data.settings;

import com.mrboomdev.awery.sdk.util.Callbacks;

public interface ListenableSettingsItem {

	void setNewItemListener(Callbacks.Callback2<SettingsItem, Integer> listener);

	void setRemovalItemListener(Callbacks.Callback2<SettingsItem, Integer> listener);

	void setChangeItemListener(Callbacks.Callback2<SettingsItem, Integer> listener);

	void onNewItem(SettingsItem item, int position);

	void onRemoval(SettingsItem item, int position);

	void onChange(SettingsItem item, int position);
}