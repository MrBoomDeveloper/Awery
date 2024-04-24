package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.sdk.util.exceptions.InvalidSyntaxException;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An utility class for working with shared preferences, which contains constant key names for quick access.
 * @author MrBoomDev
 */
public class AwerySettings {
	private static final Map<String, SettingsItem> cachedPaths = new HashMap<>();
	private static final String TAG = "AwerySettings";
	public static final String APP_SETTINGS = "Awery";
	private static boolean shouldReloadMapValues;
	private static SettingsItem settingsMapInstance;
	private final SharedPreferences prefs;
	private final Context context;
	private SharedPreferences.Editor editor;

	public static final String VERBOSE_NETWORK = "settings_advanced_log_network";
	public static final String LAST_OPENED_VERSION = "last_opened_version";

	public enum AdultContentMode {
		ENABLED, DISABLED, ONLY
	}

	public static final class theme {
		public static final String USE_MATERIAL_YOU = "settings_theme_use_material_you";
		public static final String DARK_THEME = "settings_theme_dark_theme";
		public static final String EXTRACT_COVER_COLORS = "settings_theme_use_source_theme";
		public static final String THEME_PALLET = "settings_theme_pallet";
		public static final String USE_OLED = "settings_theme_amoled";
	}

	public static final class ui {
		public static final String DEFAULT_HOME_TAB = "settings_ui_default_tab";
	}

	public static final class content {
		public static final String HIDE_LIBRARY_ENTRIES = "settings_content_hide_library_entries";
		public static final String GLOBAL_EXCLUDED_TAGS = "settings_content_global_excluded_tags";
		public static final String ADULT_CONTENT = "settings_content_adult_content_mode";
	}

	public static final class player {
		public static final String GESTURES_MODE = "settings_player_gestures";
		public static final String BIG_SEEK_LENGTH = "settings_player_big_seek_length";
		public static final String DOUBLE_TAP_SEEK_LENGTH = "settings_player_double_tap_seek_length";
	}

	private AwerySettings(@NonNull Context context, String name) {
		this.prefs = context.getSharedPreferences(name, 0);
		this.context = context;
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

	private static void reloadSettingsMapValues(Context context) {
		settingsMapInstance.restoreValues(AwerySettings.getInstance(context));
		shouldReloadMapValues = false;
	}

	public static SettingsItem getSettingsMap(Context context) {
		if(settingsMapInstance != null) {
			if(shouldReloadMapValues) {
				reloadSettingsMapValues(context);
			}

			return settingsMapInstance;
		}

		try(var reader = new BufferedReader(new InputStreamReader(context.getAssets().open("settings.json"), StandardCharsets.UTF_8))) {
			var builder = new StringBuilder();
			String line;

			while((line = reader.readLine()) != null) {
				builder.append(line);
			}

			try {
				var moshi = new Moshi.Builder().build();
				var adapter = moshi.adapter(SettingsItem.class);
				settingsMapInstance = adapter.fromJson(builder.toString());

				if(settingsMapInstance == null) {
					throw new IllegalStateException("Failed to parse settings");
				}

				settingsMapInstance.setAsParentForChildren();
				reloadSettingsMapValues(context);
				return settingsMapInstance;
			} catch(IOException e) {
				throw new InvalidSyntaxException("Failed to parse settings", e);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see #getBoolean(String)
	 * @return the value of the specified key, or the default value if the key does not exist
	 * @author MrBoomDev
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		if(!prefs.contains(key)) {
			checkEditorExistence().putBoolean(key, defaultValue);
			saveSync();
			return defaultValue;
		}

		return prefs.getBoolean(key, defaultValue);
	}

	/**
	 * @return whether the specified key exists
	 * @author MrBoomDev
	 */
	public boolean contains(String key) {
		return prefs.contains(key);
	}

	/**
	 * @see #getBoolean(String, boolean)
	 * @return the value of the specified key or false if the key does not exist
	 * @author MrBoomDev
	 */
	public boolean getBoolean(String key) {
		var found = getSettingsMap(context).find(key);

		if(found != null) {
			var value = found.getBooleanValue();
			if(value != null) return value;
		}

		return getBoolean(key, false);
	}

	public AwerySettings setBoolean(String key, boolean value) {
		checkEditorExistence().putBoolean(key, value);
		return this;
	}

	public int getInt(String key, int defaultValue) {
		if(!prefs.contains(key)) {
			checkEditorExistence().putInt(key, defaultValue);
			saveSync();
			return defaultValue;
		}

		return prefs.getInt(key, defaultValue);
	}

	public Integer getInt(String key) {
		var found = getSettingsMap(context).find(key);

		if(found != null) {
			var value = found.getIntValue();
			if(value != null) return value;
		}

		return getInt(key, 0);
	}

	public AwerySettings setInt(String key, int value) {
		checkEditorExistence().putInt(key, value);
		return this;
	}

	public String getString(String key, String defaultValue) {
		if(!prefs.contains(key)) {
			checkEditorExistence().putString(key, defaultValue);
			saveSync();
			return defaultValue;
		}

		return prefs.getString(key, defaultValue);
	}

	public String getString(String key) {
		var found = getSettingsMap(context).find(key);

		if(found != null) {
			var value = found.getStringValue();
			if(value != null) return value;
		}

		return getString(key, null);
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
		var found = getSettingsMap(context).find(key);

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

	public AwerySettings setString(String key, String value) {
		checkEditorExistence().putString(key, value);
		return this;
	}

	public Set<String> getStringSet(String name) {
		if(contains(name)) {
			return prefs.getStringSet(name, null);
		}

		return prefs.getStringSet(name, new HashSet<>());
	}

	public AwerySettings setStringSet(String key, Set<String> value) {
		checkEditorExistence().putStringSet(key, value);
		return this;
	}

	private SharedPreferences.Editor checkEditorExistence() {
		if(editor == null) {
			editor = prefs.edit();
		}

		return editor;
	}

	/**
	 * Saves the changes to the shared preferences asynchronously.
	 * @return this instance for chaining methods
	 * @see #saveSync()
	 * @author MrBoomDev
	 */
	public AwerySettings saveAsync() {
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
	public AwerySettings saveSync() {
		if(editor == null) {
			return this;
		}

		editor.commit();
		editor = null;
		shouldReloadMapValues = true;
		return this;
	}

	/**
	 * @param context Application context
	 * @param name the name of file
	 * @return the singleton instance of {@link AwerySettings}
	 * @author MrBoomDev
	 * @see #getInstance()
	 * @see #getInstance(Context)
	 * @see #getInstance(String)
	 */
	@NonNull
	public static AwerySettings getInstance(@NonNull Context context, String name) {
		return new AwerySettings(context, name);
	}

	/**
	 * @param context Application context
	 * @return the singleton instance of {@link AwerySettings}
	 * @author MrBoomDev
	 * @see #getInstance()
	 * @see #getInstance(String)
	 * @see #getInstance(Context, String)
	 */
	@NonNull
	public static AwerySettings getInstance(@NonNull Context context) {
		return getInstance(context, APP_SETTINGS);
	}

	/**
	 * @param name the name of file
	 * @return the singleton instance of {@link AwerySettings}
	 * @see #getInstance()
	 * @see #getInstance(Context)
	 * @see #getInstance(Context, String)
	 * @author MrBoomDev
	 */
	@NonNull
	public static AwerySettings getInstance(String name) {
		return getInstance(getAnyContext(), name);
	}

	/**
	 * @return the singleton instance of {@link AwerySettings}
	 * @see #getInstance(String)
	 * @see #getInstance(Context)
	 * @see #getInstance(Context, String)
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract(" -> new")
	public static AwerySettings getInstance() {
		return getInstance(APP_SETTINGS);
	}
}