package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.AweryApp.copyToClipboard;
import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.showLoadingWindow;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.cleanString;
import static com.mrboomdev.awery.util.NiceUtils.cleanUrl;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.isTrue;
import static com.mrboomdev.awery.util.NiceUtils.isUrlValid;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.common.util.concurrent.FutureCallback;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.db.item.DBRepository;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.LazySettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.fields.EditTextField;
import com.squareup.moshi.Json;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java9.util.Objects;

public class ExtensionSettings extends SettingsItem implements SettingsDataHandler, ObservableSettingsItem {
	private static final String TAG = "ExtensionSettings";
	private final ActivityResultLauncher<String> pickLauncher;
	private final List<SettingsItem> headerItems = new ArrayList<>();
	private final ExtensionsManager manager;
	@Json(ignore = true)
	private final Activity activity;
	private List<DBRepository> repos;
	private List<Extension> extensions;
	private DialogBuilder currentDialog;

	private final SettingsItem reposHeader = new SettingsItem.Builder(
			SettingsItemType.CATEGORY).setTitle("Repositories").build();

	private final SettingsItem extensionsHeader = new SettingsItem.Builder(
			SettingsItemType.CATEGORY).setTitle("Installed").build();

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
				extensions = List.copyOf(manager.getAllExtensions());
				var newItem = new ExtensionSetting(activity, extension);

				if(hasExisted) {
					toast("Extension updated successfully!");

					var oldItem = find(getItems(), item ->
							item instanceof ExtensionSetting setting &&
							setting.getExtension().getId().equals(extension.getId()));

					onSettingChange(newItem, oldItem);
				} else {
					toast("Extension installed successfully!");

					if(extensions.size() == 1) {
						onSettingAddition(extensionsHeader, repos.isEmpty() ? 0 : repos.size() + 1);
					}

					onSettingAddition(newItem, extensions.size() + (repos.isEmpty() ? 0 : repos.size() + 1));
				}

				if(currentDialog != null) {
					currentDialog.dismiss();
				}

				for(var source : extension.getProviders()) {
					if(source.hasFeatures(ExtensionProvider.FEATURE_CHANGELOG)) {
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
			} catch(JsException e) {
				Log.e(TAG, "Failed to install an extension!", e);

				if(e.getErrorId() == null || JsException.OTHER.equals(e.getErrorId())) {
					CrashHandler.showErrorDialog(activity,
							activity.getString(R.string.extension_installed_failed),
							activity.getString(R.string.please_report_bug_extension), e);
				} else {
					CrashHandler.showErrorDialog(activity, e);
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
				var inputField = new EditTextField(context, R.string.repository_url);
				inputField.setLinesCount(1);

				currentDialog = new DialogBuilder(context)
						.setTitle(R.string.add_extension)
						.addView(inputField.getView())
						.setOnDismissListener(dialog -> currentDialog = dialog)
						.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
						.setNeutralButton(R.string.pick_from_storage, dialog -> pickLauncher.launch("*/*"))
						.setPositiveButton(R.string.ok, dialog -> {
							var text = cleanUrl(inputField.getText());

							if(text.isBlank()) {
								inputField.setError(R.string.text_cant_empty);
								return;
							}

							if(!isUrlValid(text)) {
								inputField.setError(R.string.invalid_url);
								return;
							}

							inputField.setError(null);
							var loadingWindow = showLoadingWindow();

							manager.getRepository(text, (extensions, e) -> {
								if(e != null) {
									Log.e(TAG, "Failed to get a repository!", e);
									runOnUiThread(() -> inputField.setError(ExceptionDescriptor.getTitle(e, context)));
									loadingWindow.dismiss();
									return;
								}

								if(find(repos, item -> Objects.equals(item.url, text)) != null) {
									toast("Repository already exists!");
									loadingWindow.dismiss();
									return;
								}

								var dao = getDatabase().getRepositoryDao();
								var repo = new DBRepository(text, manager.getId());
								dao.add(repo);
								repos.add(repo);

								runOnUiThread(() -> {
									if(repos.size() == 1) {
										onSettingAddition(reposHeader, 0);
									}

									onSettingAddition(new RepositorySetting(repo), 1);

									dialog.dismiss();
									loadingWindow.dismiss();
									toast("Repository added successfully!");
								});
							});
						}).show();
			}
		});
	}

	public void loadData() {
		var items = new ArrayList<SettingsItem>();

		repos = getDatabase().getRepositoryDao()
				.getRepositories(manager.getId());

		if(!repos.isEmpty()) {
			items.add(reposHeader);

			items.addAll(stream(repos)
					.map(RepositorySetting::new)
					.toList());
		}

		extensions = List.copyOf(manager.getAllExtensions());

		if(!extensions.isEmpty()) {
			items.add(extensionsHeader);

			items.addAll(stream(extensions)
					.sorted()
					.map(extension -> new ExtensionSetting(activity, extension))
					.toList());
		}

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
		if(item.getType().isBoolean() && !isTrue(item.getBooleanValue())) return;
		SettingsActivity.start(activity, item);
	}

	@Override
	public void saveValue(@NonNull SettingsItem item, Object newValue) {
		var isEnabled = isTrue(newValue);

		if(item instanceof ExtensionSetting) {
			getPrefs().setValue("ext_" + manager.getId() + "_" + item.getKey() + "_enabled", isEnabled).saveAsync();
			if(isEnabled) manager.loadExtension(activity, item.getKey());
			else manager.unloadExtension(activity, item.getKey());
		} else if(item instanceof RepositorySetting repositorySetting) {
			getPrefs().setValue("repo_" + manager.getId() + "_" + repositorySetting.repository.url + "_enabled", isEnabled).saveAsync();
		}
	}

	@Override
	public Object restoreValue(@NonNull SettingsItem item) {
		if(item instanceof ExtensionSetting) {
			return getPrefs().getBoolean("ext_" + manager.getId() + "_" + item.getKey() + "_enabled", true);
		} else if(item instanceof RepositorySetting repositorySetting) {
			return getPrefs().getBoolean("repo_" + manager.getId() + "_" + repositorySetting.repository.url + "_enabled", true);
		}

		return null;
	}

	private class RepositorySetting extends CustomSettingsItem implements LazySettingsItem {
		private final DBRepository repository;
		private List<? extends SettingsItem> items;

		private final List<SettingsItem> actionItems = List.of(
				new CustomSettingsItem(new SettingsItem.Builder(SettingsItemType.ACTION)
						.setTitle("Copy to clipboard").setIcon(R.drawable.ic_share_filled).build()) {

					@Override
					public void onClick(Context context) {
						copyToClipboard(repository.url, repository.url);
					}
				},

				new CustomSettingsItem(new SettingsItem.Builder(SettingsItemType.ACTION)
						.setTitle(R.string.delete).setIcon(R.drawable.ic_delete_outlined).build()) {

					@Override
					public void onClick(Context context) {
						var window = showLoadingWindow();

						thread(() -> {
							var dao = getDatabase().getRepositoryDao();
							dao.remove(repository);
							repos.remove(repository);

							runOnUiThread(() -> {
								onSettingRemoval(RepositorySetting.this);

								if(repos.isEmpty()) {
									onSettingRemoval(reposHeader);
								}

								window.dismiss();
							});
						});
					}
				}
		);

		public RepositorySetting(DBRepository repository) {
			super(SettingsItemType.SCREEN);
			this.repository = repository;
		}

		@Override
		public String getTitle(Context context) {
			return repository.url;
		}

		@Override
		public List<SettingsItem> getActionItems() {
			return actionItems;
		}

		@Override
		public List<? extends SettingsItem> getItems() {
			return items;
		}

		@NonNull
		@Contract(" -> new")
		@Override
		public AsyncFuture<SettingsItem> loadLazily() {
			return AsyncUtils.controllableFuture(future ->
					manager.getRepository(repository.url, (extensions, throwable) -> {
						if(throwable != null) {
							future.fail(throwable);
							return;
						}

						items = stream(extensions)
								.map(RepositoryItem::new)
								.toList();

						future.complete(this);
					}));
		}
	}

	private static class RepositoryItem extends CustomSettingsItem {
		private final Extension extension;

		public RepositoryItem(Extension extension) {
			super(SettingsItemType.ACTION);
			this.extension = extension;
		}

		@Override
		public String getTitle(Context context) {
			return extension.getName();
		}

		@Override
		public String getDescription(Context context) {
			var result = extension.getVersion();

			if(extension.isNsfw()) {
				result += " (Nsfw)";
			}

			return result;
		}

		@Override
		public String getRawIcon() {
			return extension.getRawIcon();
		}

		@Override
		public boolean tintIcon() {
			return false;
		}

		@Override
		public void onClick(Context context) {
			// TODO: 7/17/2024 Download and install an extension
		}
	}

	private class ExtensionSetting extends SettingsItem implements SettingsDataHandler, ObservableSettingsItem {
		private final Extension extension;

		public ExtensionSetting(Activity activity, @NonNull Extension extension) {
			setParent(ExtensionSettings.this);
			this.extension = extension;

			var description = extension.getVersion() != null
					? ("v" + extension.getVersion()) : extension.getErrorTitle();

			if(extension.isNsfw()) {
				description += " (Nsfw)";
			}

			var items = new ArrayList<>(Arrays.asList(stream(extension.getProviders()).map(provider ->
					returnWith(AsyncUtils.<SettingsItem>awaitResult(breaker ->
							provider.getSettings(activity, new ExtensionProvider.ResponseCallback<>() {
								@Override
								public void onSuccess(SettingsItem item) {
									breaker.run(item);
								}

								@Override
								public void onFailure(Throwable e) {
									breaker.run(null);
								}
							})), settingsScreen -> {
						if(settingsScreen != null) {
							settingsScreen.setParent(this);
						}

						return settingsScreen;
					})).filter(Objects::nonNull).toArray(SettingsItem[]::new)));

			if(!items.isEmpty()) {
				items.add(new SettingsItem(SettingsItemType.DIVIDER));
			}

			items.add(new CustomSettingsItem(SettingsItemType.ACTION) {

				@Override
				public String getTitle(android.content.Context context) {
					return context.getString(R.string.uninstall_extension);
				}

				@Override
				public void onClick(android.content.Context context) {
					var window = showLoadingWindow();

					manager.uninstallExtension(context, extension.getId()).addCallback(new FutureCallback<>() {
						@Override
						public void onSuccess(Boolean result) {
							window.dismiss();

							if(result) {
								toast("Uninstalled successfully");

								doIfNotNull(getActivity(context), activity -> {
									extensions = List.copyOf(manager.getAllExtensions());
									activity.finish();

									runOnUiThread(() -> {
										ExtensionSettings.this.onSettingRemoval(ExtensionSetting.this);

										if(extensions.isEmpty()) {
											ExtensionSettings.this.onSettingRemoval(extensionsHeader);
										}
									});
								});
							}
						}

						@Override
						public void onFailure(@NonNull Throwable e) {
							window.dismiss();

							Log.e(TAG, "Failed to uninstall an extension", e);
							CrashHandler.showErrorDialog(context, "Failed to uninstall an extension", e);
						}
					});
				}
			});

			copyFrom(new Builder(SettingsItemType.SCREEN_BOOLEAN)
					.setTitle(extension.getName())
					.setDescription(description)
					.setKey(extension.getId())
					.setParent(ExtensionSettings.this)
					.setItems(items)
					.setValue(getPrefs().getBoolean(getExtensionKey(extension) + "_enabled", true))
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
			ExtensionSettings.this.onScreenLaunchRequest(item);
		}

		@Override
		public void saveValue(SettingsItem item, Object newValue) {
			ExtensionSettings.this.saveValue(item, newValue);
		}

		@Nullable
		@Contract(pure = true)
		@Override
		public Object restoreValue(SettingsItem item) {
			return ExtensionSettings.this.restoreValue(item);
		}
	}
}