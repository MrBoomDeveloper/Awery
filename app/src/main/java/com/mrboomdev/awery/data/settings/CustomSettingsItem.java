package com.mrboomdev.awery.data.settings;

import com.mrboomdev.awery.util.exceptions.UnimplementedException;

public abstract class CustomSettingsItem extends SettingsItem {

	public abstract void saveValue(Object value);

	public Object getSavedValue() {
		return switch(getType()) {
			case BOOLEAN -> getBooleanValue();
			case STRING, SELECT -> getStringValue();
			case MULTISELECT -> getStringSetValue();
			default -> throw new UnimplementedException("Unsupported type: " + getType());
		};
	}
}