package com.mrboomdev.awery.app.data.settings.base;

import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import com.caoccao.javet.annotations.V8Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Deprecated(forRemoval = true)
public class SettingsList extends ArrayList<SettingsItem> {

	public SettingsList(SettingsItem... items) {
		super(List.of(items));
	}

	public SettingsList(Collection<? extends SettingsItem> original) {
		super(original);
	}

	@V8Function
	public SettingsItem get(String key) {
		return find(this, filter -> Objects.equals(filter.getKey(), key));
	}

	@V8Function
	public SettingsItem require(String key) {
		return requireArgument(get(key), key);
	}
}