package com.mrboomdev.awery.app.data.settings.base;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.ext.data.Selection;

import java.io.Serializable;
import java.util.Set;

@Deprecated(forRemoval = true)
public class ProxySettingsDataHandler implements SettingsDataHandler {

	@Override
	public void onScreenLaunchRequest(SettingsItem item) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void saveValue(@NonNull SettingsItem item, Object newValue) {
		var type = item.getType();

		if(type == null) {
			if(newValue == null) return;
			else if(newValue instanceof String s) item.setValue(s);
			else if(newValue instanceof Selection.State s) item.setValue(s);
			else if(newValue instanceof Integer i) item.setValue(i);
			else if(newValue instanceof Long l) item.setValue(l);
			else if(newValue instanceof Boolean b) item.setValue(b);
			else if(newValue instanceof Set<?> s) item.setValue((Set<String>) s);
			else if(newValue instanceof Serializable s) item.setValue(s);

			throw new IllegalArgumentException("Unknown value type! " + newValue.getClass().getName());
		}

		switch(type) {
			case INTEGER, SELECT_INTEGER, COLOR -> item.setValue((Integer) newValue);
			case BOOLEAN, SCREEN_BOOLEAN -> item.setValue((Boolean) newValue);
			case STRING, SELECT, JSON -> item.setValue((String) newValue);
			case SERIALIZABLE -> item.setValue((Serializable) newValue);
			case EXCLUDABLE -> item.setValue((Selection.State) newValue);
			case MULTISELECT -> item.setValue((Set<String>) newValue);
			case DATE -> item.setValue((Long) newValue);
		}
	}

	@Override
	public Object restoreValue(@NonNull SettingsItem item) {
		var type = item.getType();
		if(type == null) return item.getValue();

		return switch(type) {
			case EXCLUDABLE -> item.getExcludableValue();
			case SERIALIZABLE -> item.getSerializable();
			case DATE -> item.getLongValue();
			case INTEGER, SELECT_INTEGER, COLOR -> item.getIntegerValue();
			case MULTISELECT -> item.getStringSetValue();
			case BOOLEAN, SCREEN_BOOLEAN -> item.getBooleanValue();
			case SELECT, STRING, JSON -> item.getStringValue();
			default -> null;
		};
	}
}