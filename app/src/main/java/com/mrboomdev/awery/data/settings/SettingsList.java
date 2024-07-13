package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SettingsList extends ArrayList<SettingsItem> {

	public SettingsList(SettingsItem... items) {
		super(List.of(items));
	}

	public SettingsList(Collection<SettingsItem> original) {
		super(original);
	}

	public SettingsItem get(String key) {
		return find(this, filter -> Objects.equals(filter.getKey(), key));
	}

	public SettingsItem require(String key) {
		return requireArgument(get(key), key);
	}
}