package com.mrboomdev.awery.catalog.extensions.support.yomi;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.catalog.extensions.Extension;
import com.mrboomdev.awery.catalog.extensions.ExtensionProvider;
import com.mrboomdev.awery.catalog.extensions.ExtensionsManager;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import dalvik.system.PathClassLoader;

public abstract class YomiManager extends ExtensionsManager {
	private static final int PM_FLAGS = PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA;
	private final Map<String, Extension> extensions = new HashMap<>();

	public abstract String getMainClassMeta();

	public abstract String getNsfwMeta();

	public abstract String getRequiredFeature();

	public abstract String getPrefix();

	public abstract double getMinVersion();

	public abstract double getMaxVersion();

	public abstract List<ExtensionProvider> createProviders(Extension extension, Object main);

	@Override
	public Extension getExtension(String id) {
		return extensions.get(id);
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return extensions.values();
	}

	public abstract int getFlags();

	@Override
	public void initAll(@NonNull Context context) {
		var pm = context.getPackageManager();

		var packages = pm.getInstalledPackages(PM_FLAGS)
				.stream()
				.filter(p -> {
					if(p.reqFeatures == null) return false;

					for(var feature : p.reqFeatures) {
						if(feature.name == null) continue;
						if(feature.name.equals(getRequiredFeature())) return true;
					}

					return false;
				})
				.collect(Collectors.toList());

		for(var pkg : packages) {
			var label = pkg.applicationInfo.loadLabel(pm).toString();

			if(label.startsWith(getPrefix())) {
				label = label.substring(getPrefix().length()).trim();
			}

			var isNsfw = pkg.applicationInfo.metaData.getInt(getNsfwMeta(), 0) == 1;
			var extension = new Extension(pkg.packageName, label, pkg.versionName);
			extension.addFlags(getFlags());

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

			init(context, pkg.packageName);
		}
	}

	@Override
	public void init(Context context, String id) {
		var extension = extensions.get(id);
		if(extension == null) return;

		var mains = loadMains(context, extension);
		if(mains == null) return;

		var providers = mains.stream()
				.map(main -> createProviders(extension, main))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		for(var provider : providers) {
			extension.addProvider(provider);
		}
	}

	public List<?> loadMains(Context context, Extension extension) {
		var classes = loadClasses(context, extension);
		if(classes == null) return null;

		return classes.stream().map(clazz -> {
			try {
				var constructor = clazz.getConstructor();
				return constructor.newInstance();
			} catch(NoSuchMethodException e) {
				throw new RuntimeException("Failed to get a default constructor!", e);
			} catch(InvocationTargetException e) {
				throw new RuntimeException("Exception was thrown by a constructor!", e);
			} catch(IllegalAccessException e) {
				throw new RuntimeException("Default constructor is inaccessible!", e);
			} catch(InstantiationException e) {
				throw new RuntimeException("Requested class cannot be instanciated!", e);
			} catch(Throwable e) {
				throw new RuntimeException("Unknown exception occurred!", e);
			}
		}).collect(Collectors.toList());
	}

	@Nullable
	public List<Class<?>> loadClasses(@NonNull Context context, @NonNull Extension extension) {
		AtomicReference<Exception> exception = new AtomicReference<>();
		ClassLoader classLoader;
		PackageInfo pkgInfo;

		try {
			pkgInfo = context.getPackageManager().getPackageInfo(extension.getId(), PM_FLAGS);
		} catch(PackageManager.NameNotFoundException e) {
			extension.setError("Package not found!", e);
			return null;
		}

		try {
			classLoader = new PathClassLoader(
					pkgInfo.applicationInfo.sourceDir,
					null,
					context.getClassLoader());
		} catch(Throwable e) {
			extension.setError("Failed to load a ClassLoader!", e);
			return null;
		}

		var mainClassesString = pkgInfo.applicationInfo.metaData.getString(getMainClassMeta());
		if(mainClassesString == null) return null;

		List<Class<?>> classes = Arrays.stream(mainClassesString.split(";")).map(mainClass -> {
			if(mainClass.startsWith(".")) {
				mainClass = pkgInfo.packageName + mainClass;
			}

			try {
				return Class.forName(mainClass, false, classLoader);
			} catch(ClassNotFoundException e) {
				exception.set(e);
				return null;
			}
		}).filter(Objects::nonNull)
				.collect(Collectors.toList());

		if(classes.isEmpty()) {
			var e = exception.get();

			if(e != null) {
				extension.setError("Failed to load classes!", e);
			}

			return null;
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
}