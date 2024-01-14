package com.mrboomdev.awery.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;

import ani.awery.App;

public class DataPreferences {
	public static final String AWERY_SETTINGS = "Awery";
	public static final String GLOBAL_EXCLUDED_TAGS = "GLOBAL_EXCLUDED_TAGS";
	public static final String USE_MATERIAL_YOU = "use_material_you";
	public static final String USE_CUSTOM_THEME = "use_custom_theme";
	public static final String USE_SOURCE_THEME = "use_source_theme";
	public static final String CUSTOM_THEME_INT = "custom_theme_int";
	public static final String COLOR_OVERFLOW = "colorOverflow";
	public static final String USE_OLDED = "use_oled";
	public static final String THEME = "theme";
	private final SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	public DataPreferences(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		return prefs.getBoolean(name, defaultValue);
	}

	public boolean getBoolean(String name) {
		return getBoolean(name, false);
	}

	public DataPreferences setBoolean(String key, boolean value) {
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

	public DataPreferences setInt(String key, int value) {
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

	public DataPreferences setString(String key, String value) {
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

	public DataPreferences setStringSet(String key, Set<String> value) {
		checkEditorExistence();
		editor.putStringSet(key, value);
		return this;
	}

	private void checkEditorExistence() {
		if(editor == null) {
			editor = prefs.edit();
		}
	}

	public DataPreferences saveAsync() {
		if(editor == null) {
			return this;
		}

		editor.apply();
		save();
		return this;
	}

	public DataPreferences saveSync() {
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
	public static DataPreferences getInstance(@NonNull Context context, String name) {
		return new DataPreferences(context.getSharedPreferences(name, 0));
	}

	@NonNull
	public static DataPreferences getInstance(@NonNull Context context) {
		return getInstance(context, AWERY_SETTINGS);
	}

	@NonNull
	public static DataPreferences getInstance(String name) {
		var activity = App.Companion.currentActivity();

		if(activity == null) {
			throw new IllegalStateException("Failed to get a current activity!");
		}

		return getInstance(activity, name);
	}

	@NonNull
	@Contract(" -> new")
	public static DataPreferences getInstance() {
		return getInstance(AWERY_SETTINGS);
	}

	public static SharedPreferences getPreferences(String fileName) {
		var activity = App.Companion.currentActivity();

		if(activity == null) {
			throw new IllegalStateException("Failed to get a current activity!");
		}

		return activity.getSharedPreferences(fileName, 0);
	}

	public static SharedPreferences getPreferences() {
		return getPreferences(AWERY_SETTINGS);
	}
}