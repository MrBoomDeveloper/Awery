package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.squareup.moshi.Json;

import java.util.concurrent.atomic.AtomicReference;

import java9.util.Objects;

public class ExtensionSettings extends SettingsItem implements SettingsDataHandler {
	@Json(ignore = true)
	private final Activity activity;
	private final ExtensionsManager manager;

	public ExtensionSettings(AppCompatActivity activity, @NonNull ExtensionsManager manager) {
		copyFrom(new Builder(SettingsItemType.SCREEN)
				.setTitle("Aniyomi extensions")
				.setItems(stream(manager.getExtensions(0)).sorted()
						.map(extension -> new ExtensionSetting(activity, extension, new Builder(SettingsItemType.SCREEN_BOOLEAN)
								.setTitle(extension.getName())
								.setDescription("v" + extension.getVersion())
								.setKey(extension.getId())
								.setItems(stream(extension.getProviders()).map(provider -> {
									var response = new AtomicReference<SettingsItem>();

									provider.getSettings(activity, new ExtensionProvider.ResponseCallback<>() {
										@Override
										public void onSuccess(SettingsItem item) {
											response.set(Objects.requireNonNullElse(item, SettingsItem.INVALID_SETTING));
										}

										@Override
										public void onFailure(Throwable e) {
											response.set(SettingsItem.INVALID_SETTING);
										}
									});

									try {
										AweryApp.wait(() -> response.get() == null);
									} catch(InterruptedException e) {
										throw new RuntimeException(e);
									}

									return response.get() == SettingsItem.INVALID_SETTING ? null : response.get();
								}).filter(Objects::nonNull).toList())
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
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		if(!item.getBooleanValue()) return;

		var cachedKey = "ext_" + manager.getId() + "_" + item.getKey();
		item.restoreValues();

		AwerySettings.cachePath(cachedKey, item);

		var intent = new Intent(activity, SettingsActivity.class);
		intent.putExtra("path", cachedKey);
		activity.startActivity(intent);
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

	private class ExtensionSetting extends SettingsItem implements SettingsDataHandler {
		private final Extension extension;
		@Json(ignore = true)
		private final Activity activity;

		public ExtensionSetting(Activity activity, Extension extension, SettingsItem item) {
			super(item);
			this.extension = extension;
			this.activity = activity;
		}

		@Override
		public void onScreenLaunchRequest(@NonNull SettingsItem item) {
			var cachedKey = "ext_" + manager.getId() + "_" + item.getKey();
			item.restoreValues();

			AwerySettings.cachePath(cachedKey, item);

			var intent = new Intent(activity, SettingsActivity.class);
			intent.putExtra("path", cachedKey);
			activity.startActivity(intent);
		}

		@Override
		public void save(SettingsItem item, Object newValue) {

		}
	}
}