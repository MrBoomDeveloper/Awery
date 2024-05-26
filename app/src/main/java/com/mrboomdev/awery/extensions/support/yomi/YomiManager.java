package com.mrboomdev.awery.extensions.support.yomi;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionSettings;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.io.HttpClient;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import dalvik.system.PathClassLoader;
import java9.util.Objects;
import java9.util.stream.StreamSupport;

public abstract class YomiManager extends ExtensionsManager {
	private static final int PM_FLAGS = PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA;
	private final Map<String, Extension> extensions = new HashMap<>();
	private static final String TAG = "YomiManager";

	public abstract String getMainClassMeta();

	public abstract String getNsfwMeta();

	public abstract String getRequiredFeature();

	public abstract String getPrefix();

	public abstract double getMinVersion();

	public abstract double getMaxVersion();

	public abstract Collection<Integer> getBaseFeatures();

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
	public void loadAllExtensions(@NonNull Context context) {
		var pm = context.getPackageManager();

		var packages = stream(pm.getInstalledPackages(PM_FLAGS))
				.filter(p -> {
					if(p.reqFeatures == null) return false;

					for(var feature : p.reqFeatures) {
						if(feature.name == null) continue;
						if(feature.name.equals(getRequiredFeature())) return true;
					}

					return false;
				}).toList();

		for(var pkg : packages) {
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
				continue;
			}

			loadExtension(context, pkg.packageName);
		}
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
		if(!AwerySettings.getInstance().getBoolean(key, true)) {
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

	@Override
	public MimeTypes[] getExtensionMimeTypes() {
		return new MimeTypes[] { MimeTypes.APK };
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
	public void getRepository(String url, @NonNull Callbacks.Errorable<List<Extension>, Throwable> callback) {
		HttpClient.get(url).callAsync(getAnyContext(), new HttpClient.HttpCallback() {

			@Override
			public void onResponse(HttpClient.HttpResponse response) {
				var moshi = new Moshi.Builder().build();
				var adapter = moshi.<List<YomiRepoItem>>adapter(Types.newParameterizedType(List.class, YomiRepoItem.class));

				try {
					var list = adapter.fromJson(response.getText());

					if(list == null) {
						callback.onResult(null, new ZeroResultsException(
								"No extensions found", R.string.no_extensions_found));

						return;
					}

					callback.onResult(stream(list)
							.map(item -> item.toExtension(YomiManager.this))
							.toList(), null);
				} catch(IOException e) {
					callback.onResult(null, e);
				}
			}

			@Override
			public void onError(Throwable exception) {
				callback.onResult(null, exception);
			}
		});
	}
}