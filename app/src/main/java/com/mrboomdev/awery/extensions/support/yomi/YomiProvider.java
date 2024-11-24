package com.mrboomdev.awery.extensions.support.yomi;

import static com.mrboomdev.awery.util.NiceUtils.find;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class YomiProvider extends ExtensionProvider {

	public YomiProvider(Extension extension) {
		super(extension);
	}

	public abstract YomiManager getManager();

	/**
	 * @throws UnsupportedOperationException If the source doesn't implement an Configurable interface
	 * @author MrBoomDev
	 */
	protected abstract void setupPreferenceScreen(PreferenceScreen screen) throws UnsupportedOperationException;

	@NonNull
	public static String concatLink(@NonNull String domain, String other) {
		if(domain.endsWith("/")) {
			domain = domain.substring(0, domain.length() - 1);
		}

		if(other.startsWith("/")) {
			other = other.substring(1);
		}

		return domain + "/" + other;
	}

	@Override
	@SuppressLint("RestrictedApi")
	public void getSettings(Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		var manager = new PreferenceManager(context);
		manager.setSharedPreferencesName("source_" + getId());
		var screen = manager.createPreferenceScreen(context);

		try {
			setupPreferenceScreen(screen);
		} catch(UnsupportedOperationException e) {
			callback.onFailure(e);
			return;
		}

		if(screen.getPreferenceCount() == 0) {
			callback.onFailure(new IllegalStateException("Extension doesn't support settings!"));
			return;
		}

		var items = new ArrayList<SettingsItem>();

		for(int i = 0; i < screen.getPreferenceCount(); i++) {
			var preference = screen.getPreference(i);
			items.add(new YomiSetting(preference));
		}

		callback.onSuccess(new SettingsItem() {
			@Override
			public String getTitle(Context context) {
				return getName();
			}

			@Override
			public List<SettingsItem> getItems() {
				return items;
			}

			@Override
			public SettingsItemType getType() {
				return SettingsItemType.SCREEN;
			}
		});
	}

	private static class YomiSetting extends CustomSettingsItem {
		private final List<SettingsItem> items;
		private final Preference preference;

		public YomiSetting(Preference preference) {
			this.preference = preference;

			if(preference instanceof ListPreference listPref) {
				this.items = new ArrayList<>();
				var entries = listPref.getEntries();
				var values = listPref.getEntryValues();

				for(int index = 0; index < entries.length; index++) {
					var title = entries[index];
					var value = values[index];

					items.add(new SettingsItem.Builder()
							.setTitle(title.toString())
							.setKey(value.toString())
							.build());
				}
			} else if(preference instanceof MultiSelectListPreference multiSelectPref) {
				this.items = new ArrayList<>();
				var entries = multiSelectPref.getEntries();
				var values = multiSelectPref.getEntryValues();

				for(int index = 0; index < entries.length; index++) {
					var title = entries[index];
					var value = values[index];

					items.add(new SettingsItem.Builder()
							.setTitle(title.toString())
							.setKey(value.toString())
							.build());
				}
			} else {
				this.items = null;
			}
		}

		@NonNull
		@Override
		public String getTitle(Context context) {
			return preference.getTitle() == null ? "No title" : preference.getTitle().toString();
		}

		@Override
		public String getKey() {
			return preference.getKey();
		}

		@Nullable
		@Override
		public String getDescription(Context context) {
			if(preference.getSummary() != null) {
				var description = preference.getSummary().toString().trim();

				if(getType() == SettingsItemType.STRING && Objects.equals(getStringValue(), description)) {
					return "${VALUE}";
				}

				if(getType() == SettingsItemType.SELECT && find(getItems(), item -> Objects.equals(item.getTitle(null), description)) != null) {
					return "${VALUE}";
				}

				return description;
			}

			return null;
		}

		@SuppressLint("ApplySharedPref")
		@Override
		@SuppressWarnings("unchecked")
		public void saveValue(Object value) {
			if(preference instanceof TwoStatePreference twoStatePreference) {
				twoStatePreference.setChecked((boolean) value);
			} else if(preference instanceof ListPreference listPreference) {
				listPreference.setValue((String) value);
			} else if(preference instanceof MultiSelectListPreference multiSelectListPreference) {
				multiSelectListPreference.setValues((Set<String>) value);
			} else if(preference instanceof EditTextPreference editTextPreference) {
				editTextPreference.setText((String) value);
			} else {
				throw new IllegalStateException("Unknown preference type!");
			}
		}

		@NonNull
		@Override
		public Boolean getBooleanValue() {
			if(preference instanceof TwoStatePreference switchPref) {
				return switchPref.isChecked();
			}

			throw new IllegalStateException("Unknown preference type!");
		}

		@Override
		public String getStringValue() {
			if(preference instanceof ListPreference listPref) {
				return listPref.getValue();
			} else if(preference instanceof EditTextPreference textPref) {
				return textPref.getText();
			}

			throw new IllegalStateException("Unknown preference type!");
		}

		@Override
		public Set<String> getStringSetValue() {
			if(preference instanceof MultiSelectListPreference multiSelectPref) {
				return multiSelectPref.getValues();
			}

			throw new IllegalStateException("Unknown preference type!");
		}

		@Override
		public SettingsItemType getType() {
			if(preference instanceof TwoStatePreference) {
				return SettingsItemType.BOOLEAN;
			} else if(preference instanceof ListPreference) {
				return SettingsItemType.SELECT;
			} else if(preference instanceof MultiSelectListPreference) {
				return SettingsItemType.MULTISELECT;
			} else if(preference instanceof EditTextPreference) {
				return SettingsItemType.STRING;
			}

			throw new IllegalStateException("Unknown preference type!");
		}

		@Override
		public List<SettingsItem> getItems() {
			return items;
		}
	}
}