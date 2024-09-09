package com.mrboomdev.awery.extensions.support.yomi;

import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static com.mrboomdev.awery.app.Lifecycle.getAppContext;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.asRuntimeException;
import static com.mrboomdev.awery.util.NiceUtils.getTempFile;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.ext.constants.AdultContentMode;
import com.mrboomdev.awery.ext.data.Progress;
import com.mrboomdev.awery.ext.source.Extension;
import com.mrboomdev.awery.ext.source.ExtensionsManager;
import com.mrboomdev.awery.ext.source.Repository;
import com.mrboomdev.awery.extensions.ExtensionSettings;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.exceptions.CancelledException;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpRequest;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import dalvik.system.PathClassLoader;
import java9.util.Objects;

public abstract class YomiManager extends ExtensionsManager {
	private static final int PM_FLAGS = PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA;
	private final Map<String, YomiExtension> extensions = new HashMap<>();
	private static final String TAG = "YomiManager";
	private Progress progress;

	public YomiManager() {
		YomiHelper.init(getAppContext());
	}

	public abstract String getMainClassMeta();

	public abstract String getNsfwMeta();

	public abstract String getRequiredFeature();

	public abstract String getPrefix();

	public abstract double getMinVersion();

	public abstract double getMaxVersion();

	public abstract Set<String> getBaseFeatures();

	public abstract List<? extends YomiProvider> createProviders(Extension extension, Object main);

	@Override
	public Extension getExtension(String id) {
		return extensions.get(id);
	}

	@Override
	public @NotNull Collection<? extends Extension> getAllExtensions() {
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
	public void loadAllExtensions() {
		var context = getAnyContext();

		for(var pkg : getPackages(context)) {
			initExtension(pkg, context);
		}

		progress.setCompleted();
	}

	private void initExtension(@NonNull PackageInfo pkg, @NonNull Context context) {
		var pm = context.getPackageManager();

		if(pkg.applicationInfo == null) {
			throw new NullPointerException("How?");
		}

		var label = pkg.applicationInfo.loadLabel(pm).toString();

		if(label.startsWith(getPrefix())) {
			label = label.substring(getPrefix().length()).trim();
		}

		if(pkg.versionName != null) {
			try {
				checkSupportedVersionBounds(pkg.versionName, getMinVersion(), getMaxVersion());
			} catch(IllegalArgumentException e) {
				var ext = new YomiExtension(pm, pkg, label);
				ext.setThrowable(e);
				extensions.put(pkg.packageName, ext);
				return;
			}
		}

		var extension = new YomiExtension(pm, pkg, label);

		if(pkg.applicationInfo.metaData.getInt(getNsfwMeta(), 0) == 1) {
			extension.adultContent = AdultContentMode.ONLY;
		}

		extensions.put(pkg.packageName, extension);
		loadExtension(pkg.packageName);
	}

	@Override
	public YomiExtension loadExtension(String id) {
		unloadExtension(id);

		List<?> mains;
		var extension = extensions.get(id);

		if(extension == null) {
			throw new NullPointerException("Extension " + id + " not found!");
		}

		var key = ExtensionSettings.getExtensionKey(this, extension) + "_enabled";

		if(!getPrefs().getBoolean(key, true)) {
			throw new CancelledException(key + " == true");
		}

		try {
			mains = loadMains(extension);
		} catch(Throwable t) {
			Log.e(TAG, "Failed to load main classes!", t);
			extension.setThrowable(new RuntimeException("Failed to load main classes!", t));
			throw (RuntimeException) extension.getError();
		}

		extension.providers.addAll(stream(mains)
				.map(main -> createProviders(extension, main))
				.flatMap(NiceUtils::stream)
				.toList());

		extension.isLoaded = true;
		getProgress().increment();
		return extension;
	}

	@Override
	public void unloadExtension(String id) {
		var extension = extensions.get(id);

		if(extension == null) {
			throw new NoSuchElementException(id);
		}

		if(extension.isLoaded) {
			extension.isLoaded = false;
			extension.setThrowable(null);
		}
	}

	public List<?> loadMains(Extension extension) throws PackageManager.NameNotFoundException, ClassNotFoundException {
		return stream(loadClasses(extension)).map(clazz -> {
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
			@NonNull Extension extension
	) throws PackageManager.NameNotFoundException, ClassNotFoundException, NullPointerException {
		var context = getAppContext();
		var exception = new AtomicReference<Exception>();
		var pkgInfo = context.getPackageManager().getPackageInfo(extension.getId(), PM_FLAGS);

		var classLoader = new PathClassLoader(
				requireNonNull(pkgInfo.applicationInfo).sourceDir,
				null, context.getClassLoader());

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
	public Repository getRepository(String url) {
		try {
			var response = HttpClient.fetchSync(new HttpRequest(url));

			var items = Parser.<List<Repository.Item>>fromString(Parser.getAdapter(
					List.class, Repository.Item.class), response.getText());

			return new Repository.Builder(this, url)
					.setTitle(url)
					.setItems(items)
					.build();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class UriInputStream extends InputStream {
		private final Uri uri;

		public UriInputStream(Uri uri) {
			this.uri = uri;
		}

		@Override
		public int read() throws IOException {
			throw new UnsupportedOperationException("You have to ignore this method!");
		}
	}

	private void installApk(Context context, @NonNull InputStream is, AtomicReference<Extension> ext, AtomicReference<Throwable> t) {
		var tempFile = getTempFile();

		Uri uri;

		if(is instanceof UriInputStream uriInputStream) {
			uri = uriInputStream.uri;

			try {
				is = context.getContentResolver().openInputStream(uri);

				if(is == null) {
					t.set(new IllegalStateException("The received input stream is null. HOW?"));
					return;
				}
			} catch(FileNotFoundException e) {
				t.set(e);
				return;
			}
		} else {
			t.set(new IllegalArgumentException("You had to provide an Uri as an parameter!"));
			return;
		}

		try(var os = new FileOutputStream(tempFile)) {
			var buffer = new byte[1024 * 5];
			int read;

			while((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}

			is.close();
			is = null;

			runOnUiThread(() -> {
				var intent = new Intent(Intent.ACTION_VIEW);
				intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
				intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
				intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setDataAndType(uri, MimeTypes.APK.toString());

				var pm = context.getPackageManager();
				var info = pm.getPackageArchiveInfo(tempFile.getPath(), PM_FLAGS);

				if(info == null) {
					t.set(new NullPointerException("Failed to parse an APK!"));
					return;
				}

				runOnUiThread(() -> startActivityForResult(context, intent, (resultCode, data) -> {
					switch(resultCode) {
						case Activity.RESULT_OK, Activity.RESULT_FIRST_USER -> {
							try {
								var got = pm.getPackageInfo(info.packageName, PM_FLAGS);

								if(info.versionCode != got.versionCode) {
									t.set(new IllegalStateException("Failed to install an APK!"));
									return;
								}

								initExtension(got, context);
								ext.set(getExtension(info.packageName));
							} catch(Throwable e) {
								t.set(e);
							}
						}

						case Activity.RESULT_CANCELED -> t.set(new CancelledException("Install cancelled"));
					}
				}));
			});
		} catch(IOException e) {
			t.set(e);
		}
	}

	@Override
	public Extension installExtension(InputStream is) {
		var extension = new AtomicReference<Extension>();
		var t = new AtomicReference<Throwable>();

		thread(() -> {
			var context = getAnyContext();

			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.getPackageManager().canRequestPackageInstalls()) {
				installApk(context, is, extension, t);
				return;
			}

			var settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
			settingsIntent.setData(Uri.parse("package:" + context.getPackageName()));

			runOnUiThread(() -> startActivityForResult(context, settingsIntent, (resultCode, data) -> {
				switch(resultCode) {
					case Activity.RESULT_OK -> thread(() -> installApk(context, is, extension, t));
					case Activity.RESULT_CANCELED -> t.set(new CancelledException("Permission denied!"));
					default -> t.set(new CancelledException("Failed to install an extension"));
				}
			}));
		}).await();

		while(t.get() == null && extension.get() == null);

		if(t.get() != null) {
			throw asRuntimeException(t.get());
		}

		return extension.get();
	}

	@Override
	public boolean uninstallExtension(String id) {
		var t = new AtomicReference<Throwable>();
		var did = new AtomicBoolean();

		thread(() -> {
			var activity = requireNonNull(getAnyActivity(AppCompatActivity.class));
			var intent = new Intent(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:" + id));

			runOnUiThread(() -> startActivityForResult(activity, intent, (resultCode, data) -> {
				//Ignore the resultCode, it always equal to 0

				try {
					activity.getPackageManager().getPackageInfo(id, 0);
					t.set(new UnknownError());
				} catch(PackageManager.NameNotFoundException e) {
					//App info is no longer available, so it is uninstalled.
					extensions.remove(id);
				} catch(Throwable e) {
					t.set(e);
				}

				did.set(true);
			}));
		}).await();

		while(!did.get());

		if(t.get() != null) {
			throw asRuntimeException(t.get());
		}

		return did.get();
	}
}