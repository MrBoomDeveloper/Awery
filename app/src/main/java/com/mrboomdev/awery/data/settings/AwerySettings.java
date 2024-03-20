package com.mrboomdev.awery.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;

import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;

/**
 * An utility class for working with shared preferences, which contains constant key names for quick access.
 * @author MrBoomDev
 */
public class AwerySettings {
	public static final String APP_SETTINGS = "Awery";
	public static final String APP_SECRETS = "Secrets";
	private final SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	public static final String PLAYER_GESTURES = "settings_player_gestures";
	public static final String PLAYER_BIG_SEEK = "settings_player_big_seek";
	public static final String DOUBLE_TAP_SEEK = "settings_player_double_tab_seek";
	public static final String DEFAULT_HOME_TAB = "settings_ui_default_tab";
	public static final String ADULT_CONTENT = "settings_content_adult_content";
	public static final String VERBOSE_NETWORK = "settings_advanced_log_network";
	public static final String THEME_USE_MATERIAL_YOU = "settings_theme_use_material_you";
	@Deprecated
	public static final String THEME_CUSTOM = "settings_theme_custom";
	public static final String LAST_OPENED_VERSION = "last_opened_version";
	public static final String THEME_PALLET = "settings_theme_pallet";
	public static final String THEME_USE_OLDED = "settings_theme_amoled";
	public static final String THEME_USE_COLORS_FROM_MEDIA = "settings_theme_use_source_theme";

	public static final String CONTENT_GLOBAL_EXCLUDED_TAGS = "settings_content_global_excluded_tags";

	public static final String PLAYER_DEFAULT_VIDEO_QUALITY = "settings_player_default_video_quality";

	public AwerySettings(SharedPreferences prefs) {
		this.prefs = prefs;
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
		return getInstance(AweryApp.getContext(), name);
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