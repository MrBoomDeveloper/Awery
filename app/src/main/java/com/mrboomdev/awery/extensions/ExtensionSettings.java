package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.ListenableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.ui.popup.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogEditTextField;
import com.squareup.moshi.Json;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java9.util.Objects;

public class ExtensionSettings extends SettingsItem implements SettingsDataHandler, ListenableSettingsItem {
	private static final String TAG = "ExtensionSettings";
	private final ActivityResultLauncher<String> pickLauncher;
	private final List<SettingsItem> headerItems = new ArrayList<>();
	private final ExtensionsManager manager;
	@Json(ignore = true)
	private final Activity activity;
	@Json(ignore = true)
	private Callbacks.Callback2<SettingsItem, Integer> newItemListener, editItemListener, deleteItemListener;
	private DialogBuilder currentDialog;

	public ExtensionSettings(@NonNull AppCompatActivity activity, @NonNull ExtensionsManager manager) {
		copyFrom(new Builder(SettingsItemType.SCREEN)
				.setTitle(manager.getName() + " " + activity.getString(R.string.extensions))
				.setItems(stream(manager.getExtensions(0)).sorted()
						.map(extension -> new ExtensionSetting(activity, extension))
						.toList())
				.build());

		this.activity = activity;
		this.manager = manager;

		this.pickLauncher = activity.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
			if(uri == null) return;

			/*if(!MimeTypes.test(uri.getPath(), manager.getExtensionMimeTypes())) {
				toast("Picked unsupported file type");
				return;
			}*/

			try(var stream = activity.getContentResolver().openInputStream(uri)) {
				var extension = manager.installExtension(activity, stream);
				var hasExisted = manager.hasExtension(extension.getId());

				manager.addExtension(activity, extension);
				var extensions = List.copyOf(manager.getAllExtensions());

				var setting = new ExtensionSetting(activity, extension);

				if(hasExisted) {
					toast("Extension updated successfully!");

					if(editItemListener != null) {
						editItemListener.run(setting, extensions.indexOf(extension));
					}
				} else {
					toast("Extension installed successfully!");

					if(newItemListener != null) {
						newItemListener.run(setting, extensions.indexOf(extension));
					}
				}

				if(currentDialog != null) {
					currentDialog.dismiss();
				}
			} catch(Throwable e) {
				Log.e(TAG, "Failed to load the extension!", e);
				CrashHandler.showErrorDialog(activity, e, false, null);
			}
		});

		headerItems.add(new SettingsItem() {
			@Override
			public Drawable getIcon(@NonNull Context context) {
				return AppCompatResources.getDrawable(context, R.drawable.ic_add);
			}

			@Override
			public void onClick(Context context) {
				var inputField = new DialogEditTextField(context, R.string.repository_url);

				currentDialog = new DialogBuilder(context)
						.setTitle(R.string.add_extension)
						.addField(inputField)
						.setOnDismissListener(dialog -> currentDialog = dialog)
						.setCancelButton(R.string.cancel, DialogBuilder::dismiss)
						.setNeutralButton(R.string.pick_from_storage, dialog -> pickLauncher.launch("*/*"))
						.setPositiveButton(R.string.ok, dialog -> {
							var text = inputField.getText().trim();

							if(text.isBlank()) {
								inputField.setError("Field cannot be empty");
								return;
							}

							try {
								new URL(text).toString();
								inputField.setError(null);

								toast("Extension repositories aren't done yet!");
							} catch(MalformedURLException e) {
								Log.e(TAG, "Invalid URL", e);
								inputField.setError("Invalid URL");
							}
						}).show();
			}
		});
	}

	@Override
	public List<SettingsItem> getHeaderItems() {
		return headerItems;
	}

	@NonNull
	public static String getExtensionKey(@NonNull Extension extension) {
		return "ext_" + extension.getManager().getId() + "_" + extension.getId();
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

		if(isEnabled) manager.loadExtension(activity, item.getKey());
		else manager.unloadExtension(activity, item.getKey());
	}

	@Override
	public void setNewItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
		this.newItemListener = listener;
	}

	@Override
	public void setRemovalItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
		this.deleteItemListener = listener;
	}

	@Override
	public void setChangeItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
		this.editItemListener = listener;
	}

	@Override
	public void onNewItem(SettingsItem item, int position) {
		if(newItemListener != null) {
			newItemListener.run(item, position);
		}
	}

	@Override
	public void onRemoval(SettingsItem item, int position) {
		if(deleteItemListener != null) {
			deleteItemListener.run(item, position);
		}
	}

	@Override
	public void onChange(SettingsItem item, int position) {
		if(editItemListener != null) {
			editItemListener.run(item, position);
		}
	}

	private class ExtensionSetting extends SettingsItem implements SettingsDataHandler, ListenableSettingsItem {
		private Callbacks.Callback2<SettingsItem, Integer> newItemListener, editItemListener, deleteItemListener;
		private final Extension extension;
		@Json(ignore = true)
		private final Activity activity;

		public ExtensionSetting(Activity activity, @NonNull Extension extension) {
			this.extension = extension;
			this.activity = activity;

			var description = extension.getVersion() != null
					? ("v" + extension.getVersion()) : extension.getErrorTitle();

			copyFrom(new Builder(SettingsItemType.SCREEN_BOOLEAN)
					.setTitle(extension.getName())
					.setDescription(description)
					.setKey(extension.getId())
					.setParent(ExtensionSettings.this)
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

						// Wait for response
						while(response.get() == null);

						if(response.get() == SettingsItem.INVALID_SETTING) {
							return null;
						}

						response.get().setParent(this);
						return response.get();
					}).filter(Objects::nonNull).toList())
					.setBooleanValue(AwerySettings.getInstance(activity).getBoolean(
							getExtensionKey(extension) + "_enabled", true))
					.setIcon(extension.getIcon())
					.setIconSize(1.2f)
					.setTintIcon(false)
					.build());
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
		public void save(SettingsItem item, Object newValue) {}

		@Override
		public void setNewItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
			this.newItemListener = listener;
		}

		@Override
		public void setRemovalItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
			this.deleteItemListener = listener;
		}

		@Override
		public void setChangeItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
			this.editItemListener = listener;
		}

		@Override
		public void onNewItem(SettingsItem item, int position) {
			if(newItemListener != null) {
				newItemListener.run(item, position);
			}
		}

		@Override
		public void onRemoval(SettingsItem item, int position) {
			if(deleteItemListener != null) {
				deleteItemListener.run(item, position);
			}
		}

		@Override
		public void onChange(SettingsItem item, int position) {
			if(editItemListener != null) {
				editItemListener.run(item, position);
			}
		}
	}
}