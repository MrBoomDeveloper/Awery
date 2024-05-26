package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.cleanString;
import static com.mrboomdev.awery.util.NiceUtils.findIn;
import static com.mrboomdev.awery.util.NiceUtils.stream;

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
import com.mrboomdev.awery.data.db.DBRepository;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogEditTextField;
import com.squareup.moshi.Json;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java9.util.Objects;

public class ExtensionSettings extends SettingsItem implements SettingsDataHandler, ObservableSettingsItem {
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

					var oldSetting = stream(getItems())
							.filter(item -> {
								if(item instanceof ExtensionSetting extensionSetting) {
									return extensionSetting.getExtension().getId().equals(extension.getId());
								}

								return false;
							})
							.map(item -> (ExtensionSetting) item)
							.findAny().orElse(null);

					var index = getItems().indexOf(oldSetting);

					if(editItemListener != null) {
						editItemListener.run(setting, index);
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

				for(var source : extension.getProviders()) {
					if(source.hasFeature(ExtensionProvider.FEATURE_CHANGELOG)) {
						source.getChangelog(new ExtensionProvider.ResponseCallback<>() {
							@Override
							public void onSuccess(String s) {
								runOnUiThread(() -> new DialogBuilder(activity)
										.setTitle(extension.getVersion() + " Changelog")
										.setPositiveButton(R.string.ok, DialogBuilder::dismiss)
										.setMessage(cleanString(s))
										.show());
							}

							@Override
							public void onFailure(Throwable e) {
								CrashHandler.showErrorDialog(activity, "Failed ", e);
							}
						});
					}
				}
			} catch(Throwable e) {
				Log.e(TAG, "Failed to install an extension!", e);
				CrashHandler.showErrorDialog(activity, e);
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
				inputField.setLinesCount(1);

				currentDialog = new DialogBuilder(context)
						.setTitle(R.string.add_extension)
						.addView(inputField.getView())
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
								new URL(text);
							} catch(MalformedURLException e) {
								Log.e(TAG, "Invalid URL", e);
								inputField.setError("Invalid URL");
								return;
							}

							inputField.setError(null);

							manager.getRepository(text, (extensions, e) -> {
								if(e != null) {
									Log.e(TAG, "Failed to get a repository!", e);
									inputField.setError(e.getMessage());
									return;
								}

								new Thread(() -> {
									var dao = getDatabase().getRepositoryDao();
									var repos = dao.getRepositories(manager.getId());

									if(findIn(item -> item.url.equals(text), repos) != null) {
										toast("Repository already exists!");
										return;
									}

									var repo = new DBRepository(text, manager.getId());
									dao.add(repo);

									dialog.dismiss();
									toast("Repository added successfully!");

									if(newItemListener != null) {
										runOnUiThread(() -> newItemListener.run(
												new RepositorySetting(repo), null));
									}
								}).start();
							});
						}).show();
			}
		});
	}

	public void setupLongOperations() {
		var items = new ArrayList<SettingsItem>();

		var repositories = getDatabase().getRepositoryDao()
				.getRepositories(manager.getId());

		items.addAll(stream(repositories)
				.map(RepositorySetting::new)
				.toList());

		items.addAll(stream(manager.getExtensions(0)).sorted()
				.map(extension -> new ExtensionSetting(activity, extension))
				.toList());

		copyFrom(new Builder(SettingsItemType.SCREEN)
				.setTitle(manager.getName() + " " + activity.getString(R.string.extensions))
				.setItems(items)
				.build());
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

	private class RepositorySetting extends CustomSettingsItem {
		private final DBRepository repository;

		public RepositorySetting(DBRepository repository) {
			super(SettingsItemType.ACTION);
			this.repository = repository;
		}

		@Override
		public String getTitle(Context context) {
			return repository.url;
		}

		@Override
		public void onClick(Context context) {
			new DialogBuilder(context)
					.setTitle("Delete repository")
					.setMessage("Are you sure want to delete the \"" + repository.url + "\" repository? This action cannot be undone.")
					.setCancelButton(R.string.cancel, DialogBuilder::dismiss)
					.setPositiveButton(R.string.confirm, dialog -> new Thread(() -> {
						var dao = getDatabase().getRepositoryDao();
						dao.remove(repository);

						if(deleteItemListener != null) runOnUiThread(() ->
								deleteItemListener.run(this, null));

						dialog.dismiss();
					}).start())
					.show();
		}
	}

	private class ExtensionSetting extends SettingsItem implements SettingsDataHandler, ObservableSettingsItem {
		private Callbacks.Callback2<SettingsItem, Integer> newItemListener, editItemListener, deleteItemListener;
		private final Extension extension;
		@Json(ignore = true)
		private final Activity activity;

		public ExtensionSetting(Activity activity, @NonNull Extension extension) {
			setParent(ExtensionSettings.this);
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
								if(item != null && item != SettingsItem.INVALID_SETTING) {
									item.setParent(ExtensionSetting.this);
								}

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

		public Extension getExtension() {
			return extension;
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