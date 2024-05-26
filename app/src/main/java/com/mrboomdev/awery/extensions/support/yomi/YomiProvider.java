package com.mrboomdev.awery.extensions.support.yomi;

import static com.mrboomdev.awery.app.AweryApp.toast;

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
import androidx.preference.SwitchPreferenceCompat;

import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class YomiProvider extends ExtensionProvider {

	public YomiProvider(ExtensionsManager manager, Extension extension) {
		super(manager, extension);
	}

	public abstract void setupPreferenceScreen(PreferenceScreen screen);

	@Override
	@SuppressLint("RestrictedApi")
	public void getSettings(Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		var manager = new PreferenceManager(context);
		var screen = manager.createPreferenceScreen(context);
		setupPreferenceScreen(screen);

		if(screen.getPreferenceCount() == 0) {
			callback.onFailure(new IllegalStateException("Extension doesn't support settings!"));
			return;
		}

		var items = new ArrayList<SettingsItem>();

		for(int i = 0; i < screen.getPreferenceCount(); i++) {
			var preference = screen.getPreference(i);

			if(preference instanceof SwitchPreferenceCompat switchPref) {
				items.add(new YomiSetting(SettingsItemType.BOOLEAN, preference) {

					@Override
					public void saveValue(Object value) {
						switchPref.setChecked((boolean) value);
					}

					@Override
					public Boolean getBooleanValue() {
						return switchPref.isChecked();
					}
				});
			} else if(preference instanceof ListPreference listPref) {
				var prefVariants = new ArrayList<SettingsItem>();
				var entries = listPref.getEntries();
				var values = listPref.getEntryValues();

				for(int index = 0; index < entries.length; index++) {
					var title = entries[index];
					var value = values[index];

					prefVariants.add(new SettingsItem.Builder(SettingsItemType.STRING)
							.setTitle(title.toString())
							.setKey(value.toString())
							.build());
				}

				items.add(new YomiSetting(SettingsItemType.SELECT, preference) {

					@Override
					public void saveValue(Object value) {
							listPref.setValue(value.toString());
						}

					@Override
					public List<SettingsItem> getItems() {
						return prefVariants;
					}

					@Override
					public String getStringValue() {
							return listPref.getValue();
						}
				});
			} else if(preference instanceof MultiSelectListPreference multiSelectPref) {
				var prefVariants = new ArrayList<SettingsItem>();
				var entries = multiSelectPref.getEntries();
				var values = multiSelectPref.getEntryValues();

				for(int index = 0; index < entries.length; index++) {
					var title = entries[index];
					var value = values[index];

					prefVariants.add(new SettingsItem.Builder(SettingsItemType.STRING)
							.setTitle(title.toString())
							.setKey(value.toString())
							.build());
				}

				items.add(new YomiSetting(SettingsItemType.MULTISELECT, preference) {

					@Override
					@SuppressWarnings("unchecked")
					public void saveValue(Object value) {
							multiSelectPref.setValues((Set<String>) value);
						}

					@Override
					public List<SettingsItem> getItems() {
							return prefVariants;
						}

					@Override
					public Set<String> getStringSetValue() {
							return multiSelectPref.getValues();
						}
				});
			} else if(preference instanceof EditTextPreference editTextPreference) {
				items.add(new YomiSetting(SettingsItemType.STRING, preference) {
					@Override
					public void saveValue(Object value) {
							editTextPreference.setText(value.toString());
						}

					@Override
					public String getStringValue() {
							return editTextPreference.getText();
						}
				});
			} else {
				toast("Unsupported setting: " + preference.getClass().getName());
			}
		}

		callback.onSuccess(new SettingsItem() {
			@Override
			public String getTitle(Context context) {
				return getName() + " [" + getLang() + "]";
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
		private final Preference preference;

		public YomiSetting(SettingsItemType type, Preference preference) {
			super(type);
			this.preference = preference;
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
			return preference.getSummary() == null ? null : preference.getSummary().toString().trim();
		}
	}
}