package com.mrboomdev.awery.data.settings;

import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

public abstract class CustomSettingsItem extends SettingsItem {

	public CustomSettingsItem() {}

	public CustomSettingsItem(SettingsItemType type) {
		this.type = type;
	}

	public CustomSettingsItem(SettingsItem item) {
		copyFrom(item);
	}

	public void saveValue(Object value) {
		switch(getType()) {
			case STRING, SELECT -> stringValue = (String) value;
			case BOOLEAN, SCREEN_BOOLEAN -> booleanValue = (Boolean) value;
			case INTEGER, SELECT_INTEGER -> integerValue = (Integer) value;
			case DATE -> longValue = (Long) value;
			case EXCLUDABLE -> excludableValue = (Selection.State) value;
		}
	}

	public Object getSavedValue() {
		return switch(getType()) {
			case BOOLEAN, SCREEN_BOOLEAN -> getBooleanValue();
			case STRING, SELECT -> getStringValue();
			case MULTISELECT -> getStringSetValue();
			case INTEGER, SELECT_INTEGER -> getIntegerValue();
			case DATE -> getLongValue();
			case EXCLUDABLE -> getExcludableValue();
			default -> throw new UnimplementedException("Unsupported type: " + getType());
		};
	}

	@Override
	public void restoreSavedValues() {
		switch(getType()) {
			case STRING, SELECT -> stringValue = (String) getSavedValue();
			case INTEGER, SELECT_INTEGER -> integerValue = (Integer) getSavedValue();
			case BOOLEAN, SCREEN_BOOLEAN -> booleanValue = (Boolean) getSavedValue();
			case DATE -> longValue = (Long) getSavedValue();
		}
	}
}