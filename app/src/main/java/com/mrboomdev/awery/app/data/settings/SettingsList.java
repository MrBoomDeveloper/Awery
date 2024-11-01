package com.mrboomdev.awery.app.data.settings;

import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

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

	public static final Object ADAPTER = new Object() {

		@FromJson
		public SettingsList fromJson(List<SettingsItem> list) {
			return new SettingsList(list);
		}

		@ToJson
		public List<SettingsItem> toJson(SettingsList list) {
			return list;
		}
	};
}