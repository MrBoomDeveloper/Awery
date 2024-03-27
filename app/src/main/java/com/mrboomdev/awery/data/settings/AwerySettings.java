package com.mrboomdev.awery.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.util.exceptions.InvalidSyntaxException;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * An utility class for working with shared preferences, which contains constant key names for quick access.
 * @author MrBoomDev
 */
public class AwerySettings {
	private static final WeakHashMap<String, SettingsItem> cachedPaths = new WeakHashMap<>();
	private static final String TAG = "AwerySettings";
	public static final String APP_SETTINGS = "Awery";
	private static SettingsItem settingsMapInstance;
	private final SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	public static final String PLAYER_GESTURES = "settings_player_gestures";
	public static final String PLAYER_BIG_SEEK = "settings_player_big_seek";
	public static final String DOUBLE_TAP_SEEK = "settings_player_double_tab_seek";
	public static final String DEFAULT_HOME_TAB = "settings_ui_default_tab";
	public static final String ADULT_CONTENT = "settings_content_adult_content";
	public static final String DARK_THEME = "settings_theme_dark_theme";
	public static final String VERBOSE_NETWORK = "settings_advanced_log_network";
	public static final String THEME_USE_MATERIAL_YOU = "settings_theme_use_material_you";
	public static final String LAST_OPENED_VERSION = "last_opened_version";
	public static final String THEME_PALLET = "settings_theme_pallet";
	public static final String THEME_USE_OLDED = "settings_theme_amoled";
	public static final String THEME_USE_COLORS_FROM_MEDIA = "settings_theme_use_source_theme";
	public static final String CONTENT_GLOBAL_EXCLUDED_TAGS = "settings_content_global_excluded_tags";

	private AwerySettings(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public static void cachePath(String path, SettingsItem item) {
		cachedPaths.put(path, item);
	}

	public static SettingsItem getCached(String path) {
		return cachedPaths.get(path);
	}

	public static SettingsItem getSettingsMap(Context context) {
		if(settingsMapInstance != null) return settingsMapInstance;

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
				settingsMapInstance.restoreValues(AwerySettings.getInstance(context));

				return settingsMapInstance;
			} catch(IOException e) {
				throw new InvalidSyntaxException("Failed to parse settings", e);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getDefaultString(Context context, String key) {
		var settings = getSettingsMap(context);
		return settings.find(key).getStringValue();
	}

	public static boolean getDefaultBoolean(Context context, String key) {
		var settings = getSettingsMap(context);
		return settings.find(key).getBooleanValue();
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
		return getBoolean(key, false);
	}

	public Boolean getOptionalBoolean(String key) {
		if(!prefs.contains(key)) return null;
		return getBoolean(key);
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

	public int getInt(String key) {
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
		return getString(key, null);
	}

	public AwerySettings setString(String key, String value) {
		checkEditorExistence().putString(key, value);
		return this;
	}

	public Set<String> getStringSet(String key, Set<String> defaultValue) {
		if(!prefs.contains(key)) {
			checkEditorExistence().putStringSet(key, defaultValue);
			saveSync();
			return defaultValue;
		}

		return new HashSet<>(prefs.getStringSet(key, defaultValue));
	}

	public Set<String> getStringSet(String name) {
		return getStringSet(name, new HashSet<>());
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
		return new AwerySettings(context.getSharedPreferences(name, 0));
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
		return getInstance(AweryApp.getAnyContext(), name);
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