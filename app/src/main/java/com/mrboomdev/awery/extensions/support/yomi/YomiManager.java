package com.mrboomdev.awery.extensions.support.yomi;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.AweryLifecycle.startActivityForResult;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.getTempFile;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionSettings;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.util.ContentType;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.Progress;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.async.ControllableAsyncFuture;
import com.mrboomdev.awery.util.exceptions.CancelledException;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import dalvik.system.PathClassLoader;
import java9.util.Objects;
import java9.util.stream.StreamSupport;

public abstract class YomiManager extends ExtensionsManager {
	private static final int PM_FLAGS = PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA;
	private final Map<String, Extension> extensions = new HashMap<>();
	private static final String TAG = "YomiManager";
	private Progress progress;

	public abstract String getMainClassMeta();

	public abstract String getNsfwMeta();

	public abstract String getRequiredFeature();

	public abstract String getPrefix();

	public abstract double getMinVersion();

	public abstract double getMaxVersion();

	public abstract Set<String> getBaseFeatures();

	public abstract List<? extends ExtensionProvider> createProviders(Extension extension, Object main);

	@Override
	public Extension getExtension(String id) {
		return extensions.get(id);
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return extensions.values();
	}

	@Override
	public Progress getProgress() {
		if(progress == null) {
			return progress = new Progress(getPackages(getAnyContext()).size());
		}

		return progress;
	}

	private List<PackageInfo> getPackages(@NonNull Context context) {
		return stream(context.getPackageManager().getInstalledPackages(PM_FLAGS))
				.filter(p -> {
					if(p.reqFeatures == null) return false;

					for(var feature : p.reqFeatures) {
						if(feature.name == null) continue;
						if(feature.name.equals(getRequiredFeature())) return true;
					}

					return false;
				}).toList();
	}

	@Override
	public void loadAllExtensions(@NonNull Context context) {
		for(var pkg : getPackages(context)) {
			initExtension(pkg, context);
		}

		progress.setCompleted();
	}

	private void initExtension(@NonNull PackageInfo pkg, @NonNull Context context) {
		var pm = context.getPackageManager();
		var label = pkg.applicationInfo.loadLabel(pm).toString();

		if(label.startsWith(getPrefix())) {
			label = label.substring(getPrefix().length()).trim();
		}

		var isNsfw = pkg.applicationInfo.metaData.getInt(getNsfwMeta(), 0) == 1;

		var extension = new Extension(this, pkg.packageName, label, pkg.versionName) {
			@Override
			public Drawable getIcon() {
				return pkg.applicationInfo.loadIcon(pm);
			}
		};

		if(isNsfw) {
			extension.addFlags(Extension.FLAG_NSFW);
		}

		extensions.put(pkg.packageName, extension);

		try {
			checkSupportedVersionBounds(pkg.versionName, getMinVersion(), getMaxVersion());
		} catch(IllegalArgumentException e) {
			extension.setError("Unsupported version!", e);
			return;
		}

		loadExtension(context, pkg.packageName);
	}

	@Override
	public void loadExtension(Context context, String id) {
		unloadExtension(context, id);

		List<?> mains;
		var extension = extensions.get(id);

		if(extension == null) {
			throw new NullPointerException("Extension " + id + " not found!");
		}

		var key = ExtensionSettings.getExtensionKey(extension) + "_enabled";
		if(!getPrefs().getBoolean(key, true)) {
			return;
		}

		try {
			mains = loadMains(context, extension);
		} catch(Throwable t) {
			Log.e(TAG, "Failed to load main classes!", t);
			extension.setError("Failed to load main classes!", t);
			return;
		}

		var providers = stream(mains)
				.map(main -> createProviders(extension, main))
				.flatMap(StreamSupport::stream)
				.toList();

		for(var provider : providers) {
			extension.addProvider(provider);
		}

		extension.setIsLoaded(true);
		getProgress().increment();
	}

	@Override
	public void unloadExtension(Context context, String id) {
		var extension = extensions.get(id);

		if(extension == null) {
			throw new NullPointerException("Extension " + id + " not found!");
		}

		if(!extension.isLoaded()) return;

		extension.setIsLoaded(false);
		extension.clearProviders();
		extension.removeFlags(Extension.FLAG_ERROR | Extension.FLAG_WORKING);
	}

	public List<?> loadMains(
			Context context,
			Extension extension
	) throws PackageManager.NameNotFoundException, ClassNotFoundException {
		return stream(loadClasses(context, extension)).map(clazz -> {
			try {
				var constructor = clazz.getConstructor();
				return constructor.newInstance();
			} catch(NoSuchMethodException e) {
				throw new RuntimeException("Failed to get a default constructor!", e);
			} catch(InvocationTargetException e) {
				throw new RuntimeException("Exception was thrown by a constructor!", e.getCause());
			} catch(IllegalAccessException e) {
				throw new RuntimeException("Default constructor is inaccessible!", e);
			} catch(InstantiationException e) {
				throw new RuntimeException("Requested class cannot be instanciated!", e);
			} catch(Throwable e) {
				throw new RuntimeException("Unknown exception occurred!", e);
			}
		}).toList();
	}

	public List<? extends Class<?>> loadClasses(
			@NonNull Context context,
			@NonNull Extension extension
	) throws PackageManager.NameNotFoundException, ClassNotFoundException, NullPointerException {
		var exception = new AtomicReference<Exception>();
		var pkgInfo = context.getPackageManager().getPackageInfo(extension.getId(), PM_FLAGS);

		var classLoader = new PathClassLoader(
				pkgInfo.applicationInfo.sourceDir,
				null,
				context.getClassLoader());

		var mainClassesString = pkgInfo.applicationInfo.metaData.getString(getMainClassMeta());
		if(mainClassesString == null) throw new NullPointerException("Main classes not found!");

		var classes = stream(mainClassesString.split(";")).map(mainClass -> {
			if(mainClass.startsWith(".")) {
				mainClass = pkgInfo.packageName + mainClass;
			}

			try {
				return Class.forName(mainClass, false, classLoader);
			} catch(ClassNotFoundException e) {
				exception.set(e);
				return null;
			}
		}).filter(Objects::nonNull).toList();

		if(exception.get() != null) {
			if(exception.get() instanceof ClassNotFoundException e) throw e;
			else throw new RuntimeException("Unknown exception occurred!", exception.get());
		}

		return classes;
	}

	public void checkSupportedVersionBounds(
			@NonNull String versionName,
			double minVersion,
			double maxVersion
	) throws IllegalArgumentException {
		int secondDotIndex = versionName.indexOf(".", versionName.indexOf(".") + 1);

		if(secondDotIndex != -1) {
			versionName = versionName.substring(0, secondDotIndex);
		}

		var version = Double.parseDouble(versionName);

		if(version < minVersion) {
			throw new IllegalArgumentException("Unsupported deprecated version!");
		} else if(version > maxVersion) {
			throw new IllegalArgumentException("Unsupported new version!");
		}
	}

	@Override
	public AsyncFuture<List<Extension>> getRepository(String url) {
		return thread(() -> {
			var response = HttpClient.fetchSync(new HttpRequest(url));

			var list = Parser.<List<YomiRepoItem>>fromString(
					Parser.getAdapter(List.class, YomiRepoItem.class), response.getText());

			return stream(list)
					.map(item -> item.toExtension(YomiManager.this, url))
					.toList();
		});
	}

	private void installApk(@NonNull Context context, Uri uri, ControllableAsyncFuture<Extension> future) {
		var tempFile = getTempFile();

		try(var is = context.getContentResolver().openInputStream(uri)) {
			try(var os = new FileOutputStream(tempFile)) {
				var buffer = new byte[1024 * 5];
				int read;

				while((read = requireNonNull(is).read(buffer)) != -1) {
					os.write(buffer, 0, read);
				}

				runOnUiThread(() -> {
					var intent = new Intent(Intent.ACTION_VIEW);
					intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
					intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
					intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					intent.setDataAndType(uri, ContentType.APK.getMimeType());

					var pm = context.getPackageManager();
					var info = pm.getPackageArchiveInfo(tempFile.getPath(), PM_FLAGS);

					if(info == null) {
						future.fail(new NullPointerException("Failed to parse an APK!"));
						return;
					}

					runOnUiThread(() -> startActivityForResult(context, intent, (resultCode, data) -> {
						switch(resultCode) {
							case Activity.RESULT_OK, Activity.RESULT_FIRST_USER -> {
								try {
									var got = pm.getPackageInfo(info.packageName, PM_FLAGS);

									if(info.versionCode != got.versionCode) {
										future.fail(new IllegalStateException("Failed to install an APK!"));
										return;
									}

									initExtension(got, context);
									future.complete(getExtension(info.packageName));
								} catch(Throwable e) {
									future.fail(e);
								}
							}

							case Activity.RESULT_CANCELED -> future.fail(new CancelledException("Install cancelled"));
						}
					}));
				});
			}
		} catch(IOException e) {
			future.fail(e);
		}
	}

	@Override
	public AsyncFuture<Extension> installExtension(Context context, Uri uri) {
		return AsyncUtils.controllableFuture(future -> {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.getPackageManager().canRequestPackageInstalls()) {
				installApk(context, uri, future);
				return;
			}

			var settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
			settingsIntent.setData(Uri.parse("package:" + context.getPackageName()));

			runOnUiThread(() -> startActivityForResult(context, settingsIntent, (resultCode, data) -> {
				switch(resultCode) {
					case Activity.RESULT_OK -> thread(() -> installApk(context, uri, future));
					case Activity.RESULT_CANCELED -> future.fail(new CancelledException("Permission denied!"));
					default -> future.fail(new CancelledException("Failed to install an extension"));
				}
			}));
		});
	}

	@Override
	public AsyncFuture<Boolean> uninstallExtension(@NonNull Context context, String id) {
		return AsyncUtils.controllableFuture(future -> {
			var intent = new Intent(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:" + id));

			runOnUiThread(() -> startActivityForResult(context, intent, (resultCode, data) -> {
				//Ignore the resultCode, it always equal to 0

				try {
					context.getPackageManager().getPackageInfo(id, 0);
					future.complete(false);
				} catch(PackageManager.NameNotFoundException e) {
					//App info is no longer available, so it is uninstalled.
					extensions.remove(id);

					try {
						future.complete(true);
					} catch(Throwable ex) {
						future.fail(ex);
					}
				} catch(Throwable e) {
					future.fail(e);
				}
			}));
		});
	}
}