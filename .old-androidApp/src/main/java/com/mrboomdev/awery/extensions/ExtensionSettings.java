package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.App.Companion;
import static com.mrboomdev.awery.app.App.showLoadingWindow;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.platform.PlatformResourcesKt.i18n;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.db.item.DBRepository;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.LazySettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.generated.Res;
import com.mrboomdev.awery.generated.String0_commonMainKt;
import com.mrboomdev.awery.platform.Platform;
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity;
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsDataHandler;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessageKt;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.fields.EditTextField;
import com.squareup.moshi.Json;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;

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

			manager.installExtension(activity, uri).addCallback(new AsyncFuture.Callback<>() {

				@Override
				public void onSuccess(@NonNull Extension extension) {
					var hasExisted = find(extensions, ext -> ext.getId().equals(extension.getId())) != null;

					extensions = List.copyOf(manager.getAllExtensions());
					var newItem = new ExtensionSetting(activity, extension);

					if(hasExisted) {
						Platform.INSTANCE.toast("Extension updated successfully!", 0);

						var oldItem = find(getItems(), item ->
								item instanceof ExtensionSetting setting &&
										setting.getExtension().getId().equals(extension.getId()));

						runOnUiThread(() -> onSettingChange(newItem, oldItem));
					} else {
						Platform.INSTANCE.toast("Extension installed successfully!", 0);

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

					for(var source : extension.getProviders()) {
						if(source.hasFeatures(ExtensionProvider.FEATURE_CHANGELOG)) {
							source.getChangelog().addCallback(new AsyncFuture.Callback<>() {
								@Override
								public void onSuccess(@NonNull String s) {
									runOnUiThread(() -> new DialogBuilder(activity)
											.setTitle(extension.getVersion() + " Changelog")
											.setPositiveButton(i18n(String0_commonMainKt.getNo(Res.string.INSTANCE)), DialogBuilder::dismiss)
											.setMessage(cleanString(s))
											.show());
								}

								@Override
								public void onFailure(@NonNull Throwable t) {
									CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
											.setTitle("Failed to get an Changelog")
											.setThrowable(t)
											.build());
								}
							});
						}
					}
				}

				@Override
				public void onFailure(@NonNull Throwable t) {
					Log.e(TAG, "Failed to install an extension!", t);
					
					CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
							.setTitle(i18n(String0_commonMainKt.getExtension_installed_failed(Res.string.INSTANCE)))
							.setThrowable(t)
							.build());
				}
			});
		});

		headerItems.add(new SettingsItem() {
			@Override
			public Drawable getIcon(@NonNull Context context) {
				return AppCompatResources.getDrawable(context, R.drawable.ic_add);
			}

			@Override
			public void onClick(Context context) {
				var inputField = new EditTextField(context, i18n(String0_commonMainKt.getRepository_url(Res.string.INSTANCE)));
				inputField.setLinesCount(1);

				currentDialog = new DialogBuilder(context)
						.setTitle(i18n(String0_commonMainKt.getAdd_extension(Res.string.INSTANCE)))
						.addView(inputField.getView())
						.setOnDismissListener(dialog -> currentDialog = dialog)
						.setNegativeButton(i18n(String0_commonMainKt.getCancel(Res.string.INSTANCE)), DialogBuilder::dismiss)
						.setNeutralButton(i18n(String0_commonMainKt.getPick_from_storage(Res.string.INSTANCE)), dialog -> pickLauncher.launch("*/*"))
						.setPositiveButton(i18n(String0_commonMainKt.getOk(Res.string.INSTANCE)), dialog -> {
							var text = cleanUrl(inputField.getText());

							if(text.isBlank()) {
								inputField.setError(i18n(String0_commonMainKt.getText_cant_empty(Res.string.INSTANCE)));
								return;
							}

							if(!isUrlValid(text)) {
								inputField.setError(i18n(String0_commonMainKt.getInvalid_url(Res.string.INSTANCE)));
								return;
							}

							inputField.setError(null);
							var loadingWindow = showLoadingWindow();

							manager.getRepository(text).addCallback(new AsyncFuture.Callback<>() {
								@Override
								public void onSuccess(@NonNull List<Extension> result) {
									if(find(repos, item -> Objects.equals(item.url, text)) != null) {
										Platform.INSTANCE.toast("Repository already exists!", 0);
										loadingWindow.dismiss();
										return;
									}

									var dao = App.Companion.getDatabase().getRepositoryDao();
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
										Platform.INSTANCE.toast("Repository added successfully!", 0);
									});
								}

								@Override
								public void onFailure(@NonNull Throwable t) {
									Log.e(TAG, "Failed to get a repository!", t);
									runOnUiThread(() -> inputField.setError(OkiThrowableMessageKt.explain(t).getTitle()));
									loadingWindow.dismiss();
								}
							});
						}).show();
			}
		});
	}

	public void loadData() {
		var items = new ArrayList<SettingsItem>();

		repos = App.Companion.getDatabase().getRepositoryDao()
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
				.setTitle(i18n(String0_commonMainKt.getExtensions(Res.string.INSTANCE)))
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

	private class RepositorySetting extends CustomSettingsItem implements LazySettingsItem, ObservableSettingsItem {
		private final DBRepository repository;
		private List<? extends SettingsItem> items;

		private final List<SettingsItem> actionItems = List.of(
				new CustomSettingsItem(new SettingsItem.Builder(SettingsItemType.ACTION)
						.setTitle("Copy to clipboard").setIcon(R.drawable.ic_share_filled).build()) {

					@Override
					public void onClick(Context context) {
						Companion.copyToClipboard(repository.url);
					}
				},

				new CustomSettingsItem(new SettingsItem.Builder(SettingsItemType.ACTION)
						.setTitle(i18n(String0_commonMainKt.getDelete(Res.string.INSTANCE))).setIcon(R.drawable.ic_delete_outlined).build()) {

					@Override
					public void onClick(Context context) {
						var window = showLoadingWindow();

						thread(() -> {
							var dao = App.Companion.getDatabase().getRepositoryDao();
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

	private class RepositoryItem extends CustomSettingsItem {
		private final Extension extension;
		private final List<SettingsItem> items = List.of(new CustomSettingsItem(SettingsItemType.ACTION) {

			@Override
			public void onClick(Context context) {
				var window = showLoadingWindow();
				var downloadedCallback = new AtomicReference<AsyncFuture.Callback<File>>();

				downloadedCallback.set(new AsyncFuture.Callback<>() {
					@Override
					public void onSuccess(@NonNull File file) {
						manager.installExtension(context,
								FileProvider.getUriForFile(context, BuildConfig.FILE_PROVIDER, file)
						).addCallback(new AsyncFuture.Callback<>() {

							@Override
							public void onSuccess(@NonNull Extension result) {
								window.dismiss();
								Platform.INSTANCE.toast(i18n(String0_commonMainKt.getExtension_installed_successfully(Res.string.INSTANCE)), 0);
								runOnUiThread(() -> ((RepositorySetting) RepositoryItem.this.getParent()).onSettingChange(RepositoryItem.this));
							}

							@Override
							public void onFailure(@NonNull Throwable t) {
								window.dismiss();

								if(t instanceof CancellationException) {
									Platform.INSTANCE.toast(t.getMessage(), 0);
									return;
								}
								
								Log.e(TAG, "Failed to install an extension!", t);
								
								var dialog = new DialogBuilder()
										.setTitle("Failed to install an extension")
										.setNeutralButton("Dismiss", DialogBuilder::dismiss)
										.setPositiveButton(i18n(String0_commonMainKt.getUninstall_extension(Res.string.INSTANCE)), d -> {
											var window1 = showLoadingWindow();

											manager.uninstallExtension(context, extension.getId()).addCallback(new AsyncFuture.Callback<>() {
												@Override
												public void onSuccess(Boolean result) {
													window1.dismiss();
													d.dismiss();

													try {
														downloadedCallback.get().onSuccess(file);
													} catch(Throwable e) {
														onFailure(e);
													}
												}

												@Override
												public void onFailure(@NonNull Throwable t) {
													Log.e(TAG, "Failed to uninstall an extension!", t);
													window1.dismiss();
													d.dismiss();

													if(t instanceof CancellationException) {
														Platform.INSTANCE.toast(t.getMessage(), 0);
														return;
													}

													CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
															.setTitle("Failed to uninstall an extension")
															.setThrowable(t)
															.build());
												}
											});
										});
								
								switch(getUpdateStatus()) {
									case DOWNGRADE -> dialog.setMessage("It looks like you're trying to install an older version of an extension. Try uninstalling an old version and retry again."
											+ "\n\nException:\n" + OkiThrowableMessageKt.explain(t).getMessage()).show();

									case UPDATE -> dialog.setMessage("It look's like you're trying to update an extension from a different repository. Try uninstalling an old version and retry again."
											+ "\n\nException:\n" + OkiThrowableMessageKt.explain(t).getMessage()).show();

									default -> dialog.setMessage("Something just went wrong. We don't know what, and we don't know why."
											+ "\n\nException:\n" + OkiThrowableMessageKt.explain(t).getMessage()).show();
								}
							}
						});
					}

					@Override
					public void onFailure(@NonNull Throwable t) {
						Log.e(TAG, "Failed to download an extension!", t);
						window.dismiss();
						Platform.INSTANCE.toast(OkiThrowableMessageKt.explain(t).getTitle(), 0);
					}
				});

				HttpClient.download(new HttpRequest(extension.getFileUrl()), new File(context.getCacheDir(),
								"download/extension/" + manager.getId() + "/" + extension.getId() + ".apk")
				).addCallback(downloadedCallback.get());
			}

			@Override
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_download);
			}
		});

		@NonNull
		private UpdateAvailability getUpdateStatus() {
			var found = manager.getExtension(extension.getId());
			if(found == null) return UpdateAvailability.NEW;

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

		public RepositoryItem(Extension extension) {
			super(SettingsItemType.ACTION);
			this.extension = extension;
		}

		@Override
		public List<SettingsItem> getActionItems() {
			if(!getUpdateStatus().mayDownload()) {
				return Collections.emptyList();
			}

			return items;
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

			switch(getUpdateStatus()) {
				case UPDATE -> result += " (Update available)";
				case DOWNGRADE -> result += " (Older version)";
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
			if(!getUpdateStatus().mayDownload()) {
				return;
			}

			items.get(0).onClick(context);
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
					return i18n(String0_commonMainKt.getUninstall_extension(Res.string.INSTANCE));
				}

				@Override
				public void onClick(android.content.Context context) {
					var window = showLoadingWindow();

					manager.uninstallExtension(context, extension.getId()).addCallback(new AsyncFuture.Callback<>() {
						@Override
						public void onSuccess(@NonNull Boolean result) {
							window.dismiss();

							if(result) {
								Platform.INSTANCE.toast("Uninstalled successfully", 0);

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
									.setPrefix(i18n(String0_commonMainKt.getPlease_report_bug_app(Res.string.INSTANCE)))
									.setThrowable(e)
									.build());
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