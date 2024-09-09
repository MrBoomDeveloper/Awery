package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.App.copyToClipboard;
import static com.mrboomdev.awery.app.App.i18n;
import static com.mrboomdev.awery.app.App.showLoadingWindow;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getActivity;
import static com.mrboomdev.awery.app.Lifecycle.getAppContext;
import static com.mrboomdev.awery.app.Lifecycle.requireAnyActivity;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.app.data.db.AweryDB.getDatabase;
import static com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.cleanString;
import static com.mrboomdev.awery.util.NiceUtils.cleanUrl;
import static com.mrboomdev.awery.util.NiceUtils.compareVersions;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.isTrue;
import static com.mrboomdev.awery.util.NiceUtils.isUrlValid;
import static com.mrboomdev.awery.util.NiceUtils.parseVersion;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.app.data.settings.base.CustomSettingsItem;
import com.mrboomdev.awery.app.data.settings.base.LazySetting;
import com.mrboomdev.awery.app.data.settings.base.SettingsItemType;
import com.mrboomdev.awery.ext.constants.Awery;
import com.mrboomdev.awery.ext.data.Settings;
import com.mrboomdev.awery.ext.source.Extension;
import com.mrboomdev.awery.ext.source.ExtensionsManager;
import com.mrboomdev.awery.ext.source.Repository;
import com.mrboomdev.awery.ext.data.Image;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.async.EmptyFuture;
import com.mrboomdev.awery.util.exceptions.CancelledException;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.fields.EditTextField;
import com.squareup.moshi.Json;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import java9.util.Objects;

public class ExtensionSettings extends Setting {
	private static final String TAG = "ExtensionSettings";
	private final Settings headerItems = new Settings();
	private final ExtensionsManager manager;
	@Json(ignore = true)
	private final Activity activity;
	private Settings items;
	private List<Repository> repos;
	private List<Extension> extensions;
	private DialogBuilder currentDialog;

	private final Setting reposHeader = new Setting.Builder(
			Setting.Type.CATEGORY).setTitle("Repositories").build();

	private final Setting extensionsHeader = new Setting.Builder(
			Setting.Type.CATEGORY).setTitle("Installed").build();

	@UiThread
	public ExtensionSettings(@NonNull AppCompatActivity activity, @NonNull ExtensionsManager manager) {
		this.activity = activity;
		this.manager = manager;

		headerItems.add(new Setting() {
			@Override
			public Image getIcon() {
				return new AndroidImage(activity, R.drawable.ic_add);
			}

			@Override
			public void onClick() {
				var context = requireAnyActivity(AppCompatActivity.class);

				var inputField = new EditTextField(context, R.string.repository_url);
				inputField.setLinesCount(1);

				currentDialog = new DialogBuilder(context)
						.setTitle(R.string.add_extension)
						.addView(inputField.getView())
						.setOnDismissListener(dialog -> currentDialog = dialog)
						.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
						.setNeutralButton(R.string.pick_from_storage, dialog -> pickExtension())
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

							thread(() -> {
								try {
									if(NiceUtils.find(repos, item -> Objects.equals(text, item.getUrl())) != null) {
										toast("Repository already exists!");
										loadingWindow.dismiss();
										return;
									}

									var dao = getDatabase().getRepositoryDao();
									var repo = manager.getRepository(text);
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
								} catch(Throwable t) {
									Log.e(TAG, "Failed to get a repository!", t);
									runOnUiThread(() -> inputField.setError(ExceptionDescriptor.getTitle(t)));
									loadingWindow.dismiss();
								}
							});
						}).show();
			}
		});

		loadData();
	}

	private void pickExtension() {
		var intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");

		startActivityForResult(activity, intent, (resultCode, result) -> {
			if(result == null || resultCode != Activity.RESULT_OK || result.getData() == null) return;
			var uri = result.getData();

			thread(() -> {
				try(var io = activity.getContentResolver().openInputStream(uri)) {
					var extension = manager.installExtension(io);
					var hasExisted = NiceUtils.find(extensions, ext -> ext.getId().equals(extension.getId())) != null;

					extensions = List.copyOf(manager.getAllExtensions());
					var newItem = new ExtensionSetting(activity, extension);

					if(hasExisted) {
						var oldItem = NiceUtils.find(requireNonNull(getItems()), item ->
								item instanceof ExtensionSetting setting &&
										setting.getExtension().getId().equals(extension.getId()));

						var index = getItems().indexOf(oldItem);
						((List<Setting>)getItems()).set(index, newItem);

						runOnUiThread(() -> onSettingChange(newItem, oldItem));
						toast("Extension updated successfully!");
					} else {
						toast("Extension installed successfully!");

						runOnUiThread(() -> {
							if(extensions.size() == 1) {
								onSettingAddition(extensionsHeader, repos.isEmpty() ? 0 : repos.size() + 1);
							}

							onSettingAddition(newItem, extensions.size() + (repos.isEmpty() ? 0 : repos.size() + 1));
						});
					}

					if(currentDialog != null) {
						currentDialog.dismiss();
					}

					if(extension.hasFeature(Awery.FEATURE_CHANGELOG)) {
						runOnUiThread(() -> new DialogBuilder(activity)
								.setTitle(extension.getVersion() + " Changelog")
								.setPositiveButton(R.string.ok, DialogBuilder::dismiss)
								.setMessage(cleanString(extension.getProvider().getChangelog()))
								.show());
					}
				} catch(CancelledException ignored) {}
				catch(Throwable t) {
					Log.e(TAG, "Failed to install an extension!", t);

					if(t instanceof JsException e && (e.getErrorId() == null || JsException.OTHER.equals(e.getErrorId()))) {
						CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
								.setTitle(R.string.extension_installed_failed)
								.setPrefix(R.string.please_report_bug_extension)
								.setThrowable(t)
								.build());
					} else {
						CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
								.setTitle(R.string.extension_installed_failed)
								.setPrefix(R.string.please_report_bug_app)
								.setThrowable(t)
								.build());
					}
				}
			});
		});
	}

	private void loadData() {
		items = new Settings();

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
	}

	@Override
	public @Nullable Type getType() {
		return Type.SCREEN;
	}

	@Override
	public @Nullable String getTitle() {
		return manager.getName() + " " + i18n(R.string.extensions);
	}

	@Override
	public @Nullable Settings getItems() {
		return items;
	}

	@Override
	public Settings getHeaderItems() {
		return headerItems;
	}

	@NonNull
	public static String getExtensionKey(@NonNull Extension extension) {
		return "ext_" + extension.getManager().getId() + "_" + extension.getId();
	}

	@NonNull
	public static String getExtensionKey(@NotNull ExtensionsManager manager, @NonNull Extension extension) {
		return "ext_" + manager.getId() + "_" + extension.getId();
	}

	@Override
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		if(item.getType().isBoolean() && !isTrue(item.getBooleanValue())) return;
		SettingsActivity.openSettingScreen(activity, item);
	}

	@Override
	public void saveValue(@NonNull SettingsItem item, Object newValue) {
		var isEnabled = isTrue(newValue);

		if(item instanceof ExtensionSetting) {
			var window = showLoadingWindow();

			if(isEnabled) {
				manager.loadExtension(activity, item.getKey()).addCallback(new AsyncFuture.Callback<>() {

					@Override
					public void onFailure(Throwable t) {
						Log.e(TAG, "Failed to load an extension!", t);
						toast("Failed to load an extension!");

						onFinally();
						item.setValue(false);
						getPrefs().setValue("ext_" + manager.getId() + "_" + item.getKey() + "_enabled", true).saveAsync();
						runOnUiThread(() -> getParent().onSettingChange(ExtensionSettings.this));
					}

					@Override
					public void onFinally() {
						window.dismiss();
					}
				});
			} else {
				manager.unloadExtension(item.getKey()).addCallback(new EmptyFuture.Callback() {

					@Override
					public void onFailure(Throwable t) {
						Log.e(TAG, "Failed to unload an extension!", t);
						toast("Failed to unload an extension!");

						onFinally();
						item.setValue(true);
						getPrefs().setValue("ext_" + manager.getId() + "_" + item.getKey() + "_enabled", false).saveAsync();
						runOnUiThread(() -> getParent().onSettingChange(ExtensionSettings.this));
					}

					@Override
					public void onFinally() {
						window.dismiss();
					}
				});
			}
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

	private class RepositorySetting extends Setting implements LazySetting {
		private final Repository repository;
		private Settings items;

		private final Settings actionItems = new Settings(
				new Setting(new Setting.Builder(Setting.Type.ACTION)
						.setTitle("Copy to clipboard")
						.setIcon(new AndroidImage(R.drawable.ic_share_filled)).build()) {

					@Override
					public void onClick() {
						copyToClipboard(repository.getUrl(), repository.getUrl());
					}
				},

				new Setting(new Setting.Builder(Setting.Type.ACTION)
						.setTitle(i18n(R.string.delete))
						.setIcon(new AndroidImage(R.drawable.ic_delete_outlined)).build()) {

					@Override
					public void onClick() {
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

		public RepositorySetting(Repository repository) {
			super(Setting.Type.SCREEN);
			this.repository = repository;
		}

		@NonNull
		@Override
		public String getTitle() {
			return repository.getUrl();
		}

		@Override
		public Settings getActions() {
			return actionItems;
		}

		@Override
		public Settings getItems() {
			return items;
		}

		@NonNull
		@Contract(" -> new")
		@Override
		public AsyncFuture<SettingsItem> loadLazily() {
			return manager.getRepository(repository.url).then(result -> {
				items = stream(result)
						.map(ext -> {
							var item = new RepositoryItem(ext);
							item.setParent(this);
							return item;
						})
						.toList();

				return this;
			});
		}
	}

	private class RepositoryItem extends Setting {
		private final Repository.Item extension;
		private final Settings items = new Settings(new Setting(Setting.Type.ACTION) {

			@Override
			public void onClick() {
				var window = showLoadingWindow();

				thread(() -> {
					try {
						install(window, HttpClient.downloadSync(new HttpRequest(extension.getUrl()), new File(
								getAppContext().getCacheDir(), "download/extension/" + manager.getId() + "/" + extension.getId())));
					} catch(IOException e) {
						Log.e(TAG, "Failed to download an extension!", e);
						window.dismiss();
						toast(ExceptionDescriptor.getTitle(e));
					}
				});
			}

			private void install(@NotNull Dialog window, File file) {
				var uri = FileProvider.getUriForFile(getAppContext(), BuildConfig.FILE_PROVIDER, file);

				try(var io = getAppContext().getContentResolver().openInputStream(uri)) {
					manager.installExtension(io);

					window.dismiss();
					toast(R.string.extension_installed_successfully);

					runOnUiThread(() -> requireNonNull(RepositoryItem.this.getParent())
							.onSettingChange(RepositoryItem.this));
				} catch(CancelledException e) {
					toast(e.getMessage());
					window.dismiss();
				} catch(JsException e) {
					Log.e(TAG, "Failed to install an extension!", e);

					if(e.getErrorId() == null || JsException.OTHER.equals(e.getErrorId())) {
						CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
								.setTitle(R.string.extension_installed_failed)
								.setPrefix(R.string.please_report_bug_extension)
								.setThrowable(e)
								.build());
					} else {
						CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
								.setTitle(R.string.extension_installed_failed)
								.setThrowable(e)
								.build());
					}
				} catch(Throwable t) {
					window.dismiss();
					Log.e(TAG, "Failed to install an extension!", t);

					var dialog = new DialogBuilder()
							.setTitle("Failed to install an extension")
							.setNeutralButton("Dismiss", DialogBuilder::dismiss)
							.setPositiveButton(R.string.uninstall_extension, d -> {
								var window1 = showLoadingWindow();

								thread(() -> {
									try {
										manager.uninstallExtension(extension.getId());
										d.dismiss();
										install(window1, file);
									} catch(CancelledException e) {
										toast(t.getMessage());
										window1.dismiss();
									} catch(Throwable e) {
										Log.e(TAG, "Failed to uninstall an extension!", t);
										window1.dismiss();
										d.dismiss();

										CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
												.setTitle("Failed to uninstall an extension")
												.setThrowable(t)
												.build());
									}
								});
							});

					switch(getUpdateStatus()) {
						case DOWNGRADE -> dialog.setMessage("It looks like you're trying to install an older version of an extension. Try uninstalling an old version and retry again."
								+ "\n\nException:\n" + ExceptionDescriptor.getMessage(t));

						case UPDATE -> dialog.setMessage("It look's like you're trying to update an extension from a different repository. Try uninstalling an old version and retry again."
								+ "\n\nException:\n" + ExceptionDescriptor.getMessage(t));

						default -> dialog.setMessage("Something just went wrong. We don't know what, and we don't know why."
								+ "\n\nException:\n" + ExceptionDescriptor.getMessage(t));
					}

					dialog.show();
				}
			}

			@Override
			public Image getIcon() {
				return new AndroidImage(R.drawable.ic_download);
			}
		});

		@NonNull
		private UpdateAvailability getUpdateStatus() {
			Extension found;

			try {
				found = manager.getExtension(extension.getId());
			} catch(NoSuchElementException e) {
				return UpdateAvailability.NEW;
			}

			var compared = compareVersions(parseVersion(extension.getVersion()), parseVersion(found.getVersion()));
			if(compared > 0) return UpdateAvailability.UPDATE;
			if(compared < 0) return UpdateAvailability.DOWNGRADE;
			return UpdateAvailability.SAME;
		}

		private enum UpdateAvailability {
			DOWNGRADE, SAME,

			UPDATE {
				@Override
				public boolean mayDownload() {
					return true;
				}
			},

			NEW {
				@Override
				public boolean mayDownload() {
					return true;
				}
			};

			public boolean mayDownload() {
				return false;
			}
		}

		public RepositoryItem(Repository.Item extension) {
			super(Setting.Type.ACTION);
			this.extension = extension;
		}

		@Override
		public Settings getActions() {
			if(!getUpdateStatus().mayDownload()) {
				return Settings.EMPTY;
			}

			return items;
		}

		@Override
		public String getTitle() {
			return extension.getTitle();
		}

		@Override
		public String getDescription() {
			var description = extension.getVersion();

			if(extension.getAdultContentMode() != null) {
				description += switch(extension.getAdultContentMode()) {
					case ONLY -> " (Nsfw)";
					case MARKED -> " (Nsfw can be hidden)";
					case NONE -> "";
				};
			}

			switch(getUpdateStatus()) {
				case UPDATE -> description += " (Update available)";
				case DOWNGRADE -> description += " (Older version)";
			}

			return description;
		}

		@Override
		public @Nullable Image getIcon() {
			return extension.getIcon();
		}

		@Override
		public void onClick() {
			if(!getUpdateStatus().mayDownload()) {
				return;
			}

			items.get(0).onClick();
		}
	}

	private class ExtensionSetting extends Setting {
		private final Extension extension;

		public ExtensionSetting(Activity activity, @NonNull Extension extension) {
			setParent(ExtensionSettings.this);
			this.extension = extension;

			var description = extension.getError() == null
					? ("v" + extension.getVersion()) : extension.getError();

			if(extension.getAdultContentMode() != null) {
				description += switch(extension.getAdultContentMode()) {
					case ONLY -> " (Nsfw)";
					case MARKED -> " (Nsfw can be hidden)";
					case NONE -> "";
				};
			}

			var items = new ArrayList<>(Arrays.asList(stream(extension.getProviders()).map(provider ->
					returnWith(AsyncUtils.<SettingsItem>awaitResult(breaker ->
							provider.getSettings(activity).addCallback(new AsyncFuture.Callback<>() {
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
					})).filter(Objects::nonNull).toArray(Setting[]::new)));

			if(!items.isEmpty()) {
				items.add(new Setting(Setting.Type.DIVIDER));
			}

			items.add(new Setting(Setting.Type.ACTION) {

				@Override
				public String getTitle() {
					return i18n(R.string.uninstall_extension);
				}

				@Override
				public void onClick() {
					var window = showLoadingWindow();

					manager.uninstallExtension(extension.getId()).addCallback(new AsyncFuture.Callback<>() {
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
							Log.e(TAG, "Failed to uninstall an extension", e);
							window.dismiss();

							CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
									.setTitle("Failed to uninstall an extension")
									.setPrefix(R.string.please_report_bug_app)
									.setThrowable(e)
									.build());
						}
					});
				}
			});

			copyFrom(new Builder(Setting.Type.SCREEN_BOOLEAN)
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
			if(extension.getError() != null) {
				return false;
			}

			return ExtensionSettings.this.restoreValue(item);
		}
	}
}