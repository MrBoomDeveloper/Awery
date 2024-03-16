package com.mrboomdev.awery.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;

import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;

public class AwerySettings {
	public static final String APP_SETTINGS = "Awery";
	public static final String APP_SECRETS = "Secrets";
	private final SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	public static final String ADULT_CONTENT = "settings_content_adult_content";
	public static final String VERBOSE_NETWORK = "settings_advanced_log_network";
	public static final String THEME_USE_MATERIAL_YOU = "settings_theme_use_material_you";
	@Deprecated
	public static final String THEME_CUSTOM = "settings_theme_custom";
	public static final String LAST_OPENED_VERSION = "last_opened_version";
	public static final String THEME_PALLET = "settings_theme_pallet";
	public static final String THEME_USE_OLDED = "settings_theme_amoled";
	public static final String THEME_USE_COLORS_FROM_MEDIA = "settings_theme_use_source_theme";

	public static final String UI_DEFAULT_MAIN_PAGE = "settings_ui_default_main_page";

	public static final String CONTENT_GLOBAL_EXCLUDED_TAGS = "settings_content_global_excluded_tags";

	public static final String PLAYER_DEFAULT_VIDEO_QUALITY = "settings_player_default_video_quality";

	public AwerySettings(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		return prefs.getBoolean(name, defaultValue);
	}

	public boolean getBoolean(String name) {
		return getBoolean(name, false);
	}

	public AwerySettings setBoolean(String key, boolean value) {
		checkEditorExistence();
		editor.putBoolean(key, value);
		return this;
	}

	public int getInt(String key, int defaultValue) {
		return prefs.getInt(key, defaultValue);
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public AwerySettings setInt(String key, int value) {
		checkEditorExistence();
		editor.putInt(key, value);
		return this;
	}

	public String getString(String key, String defaultValue) {
		return prefs.getString(key, defaultValue);
	}

	public String getString(String key) {
		return getString(key, "");
	}

	public AwerySettings setString(String key, String value) {
		checkEditorExistence();
		editor.putString(key, value);
		return this;
	}

	public Set<String> getStringSet(String name, Set<String> defaultValue) {
		return new HashSet<>(prefs.getStringSet(name, defaultValue));
	}

	public Set<String> getStringSet(String name) {
		return getStringSet(name, new HashSet<>());
	}

	public AwerySettings setStringSet(String key, Set<String> value) {
		checkEditorExistence();
		editor.putStringSet(key, value);
		return this;
	}

	private void checkEditorExistence() {
		if(editor == null) {
			editor = prefs.edit();
		}
	}

	public AwerySettings saveAsync() {
		if(editor == null) {
			return this;
		}

		editor.apply();
		save();
		return this;
	}

	public AwerySettings saveSync() {
		if(editor == null) {
			return this;
		}

		editor.commit();
		save();
		return this;
	}

	private void save() {
		editor = null;
	}

	@NonNull
	public static AwerySettings getInstance(@NonNull Context context, String name) {
		return new AwerySettings(context.getSharedPreferences(name, 0));
	}

	@NonNull
	public static AwerySettings getInstance(@NonNull Context context) {
		return getInstance(context, APP_SETTINGS);
	}

	@NonNull
	public static AwerySettings getInstance(String name) {
		return getInstance(AweryApp.getAnyContext(), name);
	}

	@NonNull
	@Contract(" -> new")
	public static AwerySettings getInstance() {
		return getInstance(APP_SETTINGS);
	}

	public static SharedPreferences getPreferences(String fileName) {
		return AweryApp.getAnyContext().getSharedPreferences(fileName, 0);
	}

	public static SharedPreferences getPreferences() {
		return getPreferences(APP_SETTINGS);
	}
}