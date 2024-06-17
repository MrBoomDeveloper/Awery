package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.app.AweryLifecycle.getAppContext;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.sdk.util.exceptions.InvalidSyntaxException;
import com.mrboomdev.awery.util.Parser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An utility class for working with shared preferences.
 * @author MrBoomDev
 */
public class NicePreferences {
	private static final Map<String, SettingsItem> cachedPaths = new HashMap<>();
	public static final String APP_SETTINGS = "Awery";
	private static boolean shouldReloadMapValues;
	private static SettingsItem settingsMapInstance;
	private final SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private NicePreferences(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public static void cachePath(String path, SettingsItem item) {
		cachedPaths.put(path, item);
	}

	public static SettingsItem getCached(String path) {
		return cachedPaths.get(path);
	}

	public static void clearCache() {
		cachedPaths.clear();
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
			var json = readAssets("settings.json");

			settingsMapInstance = Parser.fromString(SettingsItem.class, json);
			settingsMapInstance.setAsParentForChildren();

			reloadSettingsMapValues();
			return settingsMapInstance;
		} catch(IOException e) {
			throw new InvalidSyntaxException("Failed to parse settings", e);
		}
	}

	/**
	 * @return whether the specified key exists
	 * @author MrBoomDev
	 */
	public boolean contains(String key) {
		return prefs.contains(key);
	}

	/**
	 * @see #getBoolean(String)
	 * @return the value of the specified key, or the default value if the key does not exist
	 * @author MrBoomDev
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		if(!prefs.contains(key)) {
			if(defaultValue == null) {
				return null;
			}

			checkEditorExistence().putBoolean(key, defaultValue);
			saveSync();
			return defaultValue;
		}

		return prefs.getBoolean(key, defaultValue != null && defaultValue);
	}

	/**
	 * @see #getBoolean(String, Boolean)
	 * @return the value of the specified key or false if the key does not exist
	 * @author MrBoomDev
	 */
	public boolean getBoolean(String key) {
		if(contains(key)) {
			return getBoolean(key, null);
		}

		var found = getSettingsMap().findItem(key);

		if(found != null) {
			var value = found.getBooleanValue();
			if(value != null) return value;
		}

		return getBoolean(key, null);
	}

	public NicePreferences setBoolean(String key, boolean value) {
		checkEditorExistence().putBoolean(key, value);
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

		return prefs.getInt(key, defaultValue != null ? defaultValue : 0);
	}

	public Integer getInteger(String key) {
		if(contains(key)) {
			return getInteger(key, null);
		}

		var found = getSettingsMap().findItem(key);

		if(found != null) {
			var value = found.getIntegerValue();
			if(value != null) return value;
		}

		return getInteger(key, null);
	}

	public NicePreferences setInteger(String key, int value) {
		checkEditorExistence().putInt(key, value);
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

		return prefs.getString(key, defaultValue);
	}

	public String getString(String key) {
		var found = getSettingsMap().findItem(key);

		if(found != null) {
			var value = found.getStringValue();
			if(value != null) return value;
		}

		return getString(key, null);
	}

	public NicePreferences setString(String key, String value) {
		checkEditorExistence().putString(key, value);
		return this;
	}

	public <T extends Enum<T>> T getEnum(String key, T defaultValue, Class<T> enumClass) {
		if(defaultValue != null && !prefs.contains(key)) {
			checkEditorExistence().putString(key, defaultValue.name());
			saveSync();
			return defaultValue;
		}

		var result = prefs.getString(key, defaultValue == null ? null : defaultValue.name());
		if(result == null) return null;

		try {
			return Enum.valueOf(enumClass, result);
		} catch(IllegalArgumentException e) {
			// Enum types were changed, but the saved value links to an enum that no longer exists

			if(defaultValue != null) {
				checkEditorExistence().putString(key, defaultValue.name());
				saveSync();
			}

			return defaultValue;
		}
	}

	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
		var found = getSettingsMap().findItem(key);

		if(found != null) {
			var value = found.getStringValue();
			if(value != null) return Enum.valueOf(enumClass, value);
		}

		var saved = getEnum(key, null, enumClass);

		if(saved == null && found != null) {
			return Enum.valueOf(enumClass, found.getStringValue());
		}

		return saved;
	}

	public Set<String> getStringSet(String name) {
		if(contains(name)) {
			return prefs.getStringSet(name, null);
		}

		return prefs.getStringSet(name, new HashSet<>());
	}

	public NicePreferences setStringSet(String key, Set<String> value) {
		checkEditorExistence().putStringSet(key, value);
		return this;
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
		var context = getAppContext().getApplicationContext();
		return new NicePreferences(context.getSharedPreferences(fileName, 0));
	}

	/**
	 * @return the singleton instance of {@link NicePreferences}
	 * @author MrBoomDev
	 */
	@NonNull
	public static NicePreferences getPrefs() {
		return getPrefs(APP_SETTINGS);
	}

	public String getValue(@NonNull StringSetting setting) {
		return getString(setting.getKey());
	}

	public NicePreferences removeValue(@NonNull BaseSetting setting) {
		checkEditorExistence().remove(setting.getKey());
		return this;
	}

	public <T extends Enum<T> & EnumWithKey> NicePreferences setValue(@NonNull EnumSetting<T> setting, T value) {
		return setString(setting.getKey(), value != null ? value.getKey() : null);
	}

	public NicePreferences setValue(@NonNull StringSetting setting, String value) {
		return setString(setting.getKey(), value);
	}

	public NicePreferences setValue(@NonNull IntegerSetting setting, int value) {
		return setInteger(setting.getKey(), value);
	}

	public NicePreferences setValue(@NonNull BooleanSetting setting, boolean value) {
		return setBoolean(setting.getKey(), value);
	}

	public String getValue(@NonNull StringSetting setting, String defaultValue) {
		return getString(setting.getKey(), defaultValue);
	}

	public int getValue(@NonNull IntegerSetting setting) {
		return getInteger(setting.getKey());
	}

	public boolean getValue(@NonNull BooleanSetting setting) {
		return getBoolean(setting.getKey());
	}

	public Boolean getValue(@NonNull BooleanSetting setting, Boolean defaultValue) {
		return getBoolean(setting.getKey(), defaultValue);
	}

	public Integer getValue(@NonNull IntegerSetting setting, Integer defaultValue) {
		return getInteger(setting.getKey(), defaultValue);
	}

	@Nullable
	public <T extends Enum<T> & EnumWithKey> T getValue(@NonNull EnumSetting<T> setting) {
		var value = getString(setting.getKey());
		return parseEnum(value, setting.getValueClass());
	}

	@Nullable
	public <T extends Enum<T> & EnumWithKey> T getValue(@NonNull EnumSetting<T> setting, T defaultValue) {
		var value = getString(setting.getKey(), defaultValue != null ? defaultValue.getKey() : null);

		var result = parseEnum(value, setting.getValueClass());
		return result != null ? result : defaultValue;
	}

	@Contract("null, _ -> null; !null, null -> null")
	@Nullable
	private static <T extends Enum<T>> T parseEnum(@Nullable String string, @Nullable Class<T> enumClass) {
		if(string == null || enumClass == null) return null;

		try {
			return T.valueOf(enumClass, string);
		} catch(IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	public interface EnumWithKey {
		String getKey();
	}

	public static class EnumSetting<T extends Enum<T> & EnumWithKey> implements BaseSetting {
		private final String key;
		private final Class<T> clazz;

		public EnumSetting(String key, Class<T> clazz) {
			this.key = key;
			this.clazz = clazz;
		}

		@Nullable
		public T getValue() {
			return getPrefs().getValue(this);
		}

		public T getValue(T defaultValue) {
			return getPrefs().getValue(this, defaultValue);
		}

		public Class<T> getValueClass() {
			return clazz;
		}

		@Override
		public String getKey() {
			return key;
		}
	}

	public interface StringSetting extends BaseSetting {

		default String getValue() {
			return getPrefs().getValue(this);
		}

		default String getValue(String defaultValue) {
			return getPrefs().getValue(this, defaultValue);
		}
	}

	public interface IntegerSetting extends BaseSetting {

		default int getValue() {
			return getPrefs().getValue(this);
		}

		default Integer getValue(Integer defaultValue) {
			return getPrefs().getValue(this, defaultValue);
		}
	}

	public interface BooleanSetting extends BaseSetting {

		default boolean exists() {
			return getPrefs().contains(getKey());
		}

		default boolean getValue() {
			return getPrefs().getValue(this);
		}

		default Boolean getValue(Boolean defaultValue) {
			return getPrefs().getValue(this, defaultValue);
		}
	}

	public interface BaseSetting {
		String getKey();
	}
}