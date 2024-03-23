package com.mrboomdev.awery.extensions.support.yomi;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;

public class YomiSettings extends SettingsItem implements SettingsDataHandler {
	private final Activity activity;
	private final ExtensionsManager manager;

	public YomiSettings(Activity activity, @NonNull ExtensionsManager manager) {
		super(new SettingsItem.Builder(SettingsItemType.SCREEN)
				.setTitle("Aniyomi extensions")
				.setItems(stream(manager.getExtensions(0)).sorted()
						.map(extension -> new YomiSetting(extension, new Builder(SettingsItemType.SCREEN_BOOLEAN)
								.setTitle(extension.getName())
								.setDescription("v" + extension.getVersion())
								.setKey(extension.getId())
								.setBooleanValue(AwerySettings.getInstance(activity).getBoolean(
										getExtensionKey(extension), true))
								.setIcon(extension.getIcon())
								.setIconSize(1.2f)
								.setTintIcon(false)
								.build()))
						.toList())
				.build());

		this.activity = activity;
		this.manager = manager;
	}

	@NonNull
	public static String getExtensionKey(@NonNull Extension extension) {
		return "ext_" + extension.getManager().getId() + "_" + extension.getId() + "_enabled";
	}

	@Override
	public void onScreenLaunchRequest(SettingsItem item) {

	}

	@Override
	public void save(@NonNull SettingsItem item, Object newValue) {
		var isEnabled = (boolean) newValue;

		var prefs = AwerySettings.getInstance(activity);
		prefs.setBoolean("ext_" + manager.getId() + "_" + item.getKey() + "_enabled", isEnabled);
		prefs.saveAsync();

		if(isEnabled) manager.init(activity, item.getKey());
		else manager.unload(activity, item.getKey());
	}

	public static class YomiSetting extends SettingsItem implements SettingsDataHandler {
		private final Extension extension;

		public YomiSetting(Extension extension, SettingsItem item) {
			super(item);
			this.extension = extension;
		}

		@Override
		public void onScreenLaunchRequest(SettingsItem item) {

		}

		@Override
		public void save(SettingsItem item, Object newValue) {

		}
	}
}