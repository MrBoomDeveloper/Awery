package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.utils.FileExtensionsAndroid.readAssets;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.platform.android.AndroidGlobals;
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsDataHandler;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.Selection;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * An utility class for working with shared preferences.
 * @author MrBoomDev
 */
@Deprecated(forRemoval = true)
public class NicePreferences implements SettingsDataHandler {
	public static final String APP_SETTINGS = "Awery";
	private static boolean shouldReloadMapValues;
	private static SettingsItem settingsMapInstance;
	private final SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	@Contract(pure = true)
	private NicePreferences(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	private static void reloadSettingsMapValues() {
		settingsMapInstance.restoreSavedValues();
		shouldReloadMapValues = false;
	}

	public static SettingsItem getSettingsMap() {
		if(settingsMapInstance != null) {
			if(shouldReloadMapValues) {
				reloadSettingsMapValues();
			}

			return settingsMapInstance;
		}

		try {
			settingsMapInstance = Parser.fromString(SettingsItem.class, readAssets("settings.json"));
			settingsMapInstance.setAsParentForChildren();
			reloadSettingsMapValues();
			return settingsMapInstance;
		} catch(IOException e) {
			throw new IllegalStateException("Failed to parse settings", e);
		}
	}

	/**
	 * @return whether the specified key exists
	 * @author MrBoomDev
	 */
	public boolean contains(String key) {
		return prefs.contains(key);
	}
	
	public void remove(String key) {
		checkEditorExistence().remove(key);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return getBoolean(key, (Boolean) defaultValue);
	}

	/**
	 * @return the value of the specified key, or the default value if the key does not exist
	 * @author MrBoomDev
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		if(!contains(key)) {
			if(defaultValue == null) {
				return null;
			}

			setValue(key, defaultValue);
			saveSync();
			return defaultValue;
		}
		
		try {
			return prefs.getBoolean(key, defaultValue != null && defaultValue);
		} catch(ClassCastException e) {
			checkEditorExistence().remove(key);
			saveSync();
			return defaultValue != null && defaultValue;
		}
	}

	public NicePreferences setValue(String key, boolean value) {
		checkEditorExistence().remove(key).putBoolean(key, value);
		return this;
	}

	public Integer getInteger(String key, Integer defaultValue) {
		if(!prefs.contains(key)) {
			if(defaultValue == null) {
				return null;
			}

			checkEditorExistence().putInt(key, defaultValue);
			saveSync();
			return defaultValue;
		}

		try {
			return prefs.getInt(key, defaultValue != null ? defaultValue : 0);
		} catch(ClassCastException e) {
			checkEditorExistence().remove(key);
			saveSync();
			return defaultValue != null ? defaultValue : 0;
		}
	}

	public Long getLong(String key, Long defaultValue) {
		if(!prefs.contains(key)) {
			if(defaultValue == null) {
				return null;
			}

			checkEditorExistence().putLong(key, defaultValue);
			saveSync();
			return defaultValue;
		}
		
		try {
			return prefs.getLong(key, defaultValue != null ? defaultValue : 0);
		} catch(ClassCastException e) {
			checkEditorExistence().remove(key);
			saveSync();
			return defaultValue != null ? defaultValue : 0;
		}
	}

	public NicePreferences setValue(String key, int value) {
		checkEditorExistence().remove(key).putInt(key, value);
		return this;
	}

	public NicePreferences setValue(String key, float value) {
		checkEditorExistence().remove(key).putFloat(key, value);
		return this;
	}

	public NicePreferences setValue(String key, long value) {
		checkEditorExistence().remove(key).putLong(key, value);
		return this;
	}

	public String getString(String key, String defaultValue) {
		if(!prefs.contains(key)) {
			if(defaultValue == null) {
				return null;
			}

			checkEditorExistence().putString(key, defaultValue);
			saveSync();
			return defaultValue;
		}
		
		try {
			return prefs.getString(key, defaultValue);
		} catch(ClassCastException e) {
			checkEditorExistence().remove(key);
			saveSync();
			return defaultValue;
		}
	}

	public String getString(String key) {
		var found = getSettingsMap().findItem(key);

		if(found != null) {
			var value = found.getStringValue();
			if(value != null) return value;
		}

		return getString(key, null);
	}

	public NicePreferences setValue(String key, String value) {
		checkEditorExistence().putString(key, value);
		return this;
	}

	public Set<String> getStringSet(String name) {
		if(contains(name)) {
			return prefs.getStringSet(name, null);
		}

		return prefs.getStringSet(name, new HashSet<>());
	}

	private SharedPreferences.Editor checkEditorExistence() {
		return editor != null ? editor : (editor = prefs.edit());
	}

	/**
	 * Saves the changes to the shared preferences asynchronously.
	 * @return this instance for chaining methods
	 * @see #saveSync()
	 * @author MrBoomDev
	 */
	public NicePreferences saveAsync() {
		if(editor == null) {
			return this;
		}

		editor.apply();
		editor = null;
		shouldReloadMapValues = true;
		return this;
	}

	/**
	 * Saves the changes to the shared preferences synchronously.
	 * @return this instance for chaining methods
	 * @see #saveAsync()
	 * @author MrBoomDev
	 */
	public NicePreferences saveSync() {
		if(editor == null) {
			return this;
		}

		editor.commit();
		editor = null;
		shouldReloadMapValues = true;
		return this;
	}

	/**
	 * @return the singleton instance of {@link NicePreferences}
	 * @author MrBoomDev
	 */
	@NonNull
	public static NicePreferences getPrefs(String fileName) {
		return new NicePreferences(AndroidGlobals.applicationContext.getSharedPreferences(fileName, 0));
	}

	/**
	 * @return the singleton instance of {@link NicePreferences}
	 * @author MrBoomDev
	 */
	@NonNull
	public static NicePreferences getPrefs() {
		return getPrefs(APP_SETTINGS);
	}

	@Override
	public void onScreenLaunchRequest(SettingsItem item) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void saveValue(SettingsItem item, Object newValue) {
		if(item instanceof CustomSettingsItem custom) {
			custom.saveValue(newValue);
			return;
		}

		switch(item.getType()) {
			case BOOLEAN, SCREEN_BOOLEAN -> setValue(item.getKey(), (boolean) newValue);
			case SELECT, STRING, JSON, SERIALIZABLE -> setValue(item.getKey(), (String) newValue);
			case SELECT_INTEGER, INTEGER, COLOR -> setValue(item.getKey(), (int) newValue);
			case DATE -> setValue(item.getKey(), (long) newValue);

			case MULTISELECT -> SettingsData.saveSelectionList(item.getBehaviour(),
					(Selection<Selection.Selectable<String>>) newValue);

			default -> throw new IllegalArgumentException("Unsupported type!");
		}

		saveAsync();
	}

	@Override
	public Object restoreValue(@NonNull SettingsItem item) {
		if(item instanceof CustomSettingsItem custom) {
			return custom.getSavedValue();
		}

		if(item.getType() == null) {
			if(item.getStringValue() != null) return item.getStringValue();
			if(item.getIntegerValue() != null) return item.getIntegerValue();
			if(item.getBooleanValue() != null) return item.getBooleanValue();
			if(item.getStringSetValue() != null) return item.getStringSetValue();
			if(item.getExcludableValue() != null) return item.getExcludableValue();
			if(item.getLongValue() != null) return item.getLongValue();
			return null;
		}

		return switch(item.getType()) {
			case BOOLEAN, SCREEN_BOOLEAN -> getBoolean(item.getKey(), item.getBooleanValue());
			case DATE -> getLong(item.getKey(), item.getLongValue());
			case EXCLUDABLE -> item.getExcludableValue();
			case COLOR, INTEGER, SELECT_INTEGER -> getInteger(item.getKey(), item.getIntegerValue());
			case SELECT, STRING, JSON, SERIALIZABLE -> getString(item.getKey(), item.getStringValue());
			case MULTISELECT -> getStringSet(item.getKey());
			case DIVIDER, ACTION, SCREEN, CATEGORY -> null;
		};
	}
}