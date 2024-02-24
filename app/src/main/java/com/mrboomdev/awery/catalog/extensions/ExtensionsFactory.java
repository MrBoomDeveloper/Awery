package com.mrboomdev.awery.catalog.extensions;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.extensions.support.aniyomi.AniyomiProvider;
import com.mrboomdev.awery.catalog.extensions.support.tachiyomi.TachiyomiProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dalvik.system.PathClassLoader;
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.AnimeSource;
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory;
import eu.kanade.tachiyomi.source.MangaSource;
import eu.kanade.tachiyomi.source.SourceFactory;

public class ExtensionsFactory {
	private static final String TAG = "ExtensionFactory";
	private static final int PM_FLAGS = PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA;
	private static final String ANIYOMI_EXTENSION_FEATURE = "tachiyomi.animeextension";
	private static final String TACHIYOMI_EXTENSION_FEATURE = "tachiyomi.extension";
	private static final String TACHIYOMI_EXTENSION_NSFW = "tachiyomi.extension.nsfw";
	private static final String TACHIYOMI_EXTENSION_CLASS = "tachiyomi.extension.class";
	private static final String ANIYOMI_EXTENSION_NSFW = "tachiyomi.animeextension.nsfw";
	private static final String ANIYOMI_EXTENSION_CLASS = "tachiyomi.animeextension.class";
	private static final String ANIYOMI_PREFIX = "Aniyomi: ";
	private static final String TACHIYOMI_PREFIX = "Tachiyomi: ";
	private static final int ANIYOMI_EXTENSION_VERSION_MIN = 12;
	private static final int ANIYOMI_EXTENSION_VERSION_MAX = 15;
	private static final double TACHIYOMI_EXTENSION_VERSION_MIN = 1.2;
	private static final double TACHIYOMI_EXTENSION_VERSION_MAX = 1.5;
	private static final Map<String, Extension> extensions = new HashMap<>();

	public static void init(@NonNull Context context) {
		initTachiyomiExtensions(context);
	}

	private static void initTachiyomiExtensions(@NonNull Context context) {
		var pm = context.getPackageManager();

		var packages = pm.getInstalledPackages(PM_FLAGS)
				.stream()
				.filter(p -> {
					if(p.reqFeatures == null) return false;

					for(var feature : p.reqFeatures) {
						if(feature.name == null) continue;

						if(feature.name.equals(ANIYOMI_EXTENSION_FEATURE)) return true;
						if(feature.name.equals(TACHIYOMI_EXTENSION_FEATURE)) return true;
					}

					return false;
				})
				.collect(Collectors.toList());

		for(var pkg : packages) {
			extensions.put(pkg.packageName, initTachiyomiExtension(context, pkg, pm));
		}
	}

	@NonNull
	private static Extension initTachiyomiExtension(Context context, @NonNull PackageInfo pkg, PackageManager pm) {
		var label = pkg.applicationInfo.loadLabel(pm).toString();

		boolean isNsfw = pkg.applicationInfo.metaData.getInt(ANIYOMI_EXTENSION_NSFW, 0) == 1
				|| pkg.applicationInfo.metaData.getInt(TACHIYOMI_EXTENSION_NSFW, 0) == 1;

		if(label.startsWith(ANIYOMI_PREFIX)) label = label.substring(ANIYOMI_PREFIX.length());
		if(label.startsWith(TACHIYOMI_PREFIX)) label = label.substring(TACHIYOMI_PREFIX.length());

		var extension = new Extension(pkg.packageName, label, isNsfw, pkg.versionName);

		if(!extension.isError()) {
			try {
				initTachiyomiExtensionClasses(context, pkg, extension);
			} catch(IllegalStateException e) {
				extension.setError("Failed to init extension's classes!", e);
			}
		}

		return extension;
	}

	private static void initTachiyomiExtensionClasses(
			@NonNull Context context,
			@NonNull PackageInfo pkg,
			Extension extension
	) throws IllegalStateException {
		ClassLoader clazzLoader;
		var app = pkg.applicationInfo;

		try {
			clazzLoader = new PathClassLoader(app.sourceDir, null, context.getClassLoader());
		} catch(Exception e) {
			throw new IllegalStateException("Failed to load extension classloader!", e);
		}

		var animeExtensionClass = app.metaData.getString(ANIYOMI_EXTENSION_CLASS);
		var mangaExtensionClass = app.metaData.getString(TACHIYOMI_EXTENSION_CLASS);

		List<AnimeSource> animeSources;
		List<MangaSource> mangaSources;

		if(animeExtensionClass != null) {
			try {
				animeSources = getAnimeSources(pkg, animeExtensionClass, clazzLoader);

				for(var source : animeSources) {
					if(source instanceof AnimeCatalogueSource catalogueSource) {
						extension.isVideoExtension = true;
						extension.addProvider(new AniyomiProvider(catalogueSource));
						continue;
					}

					Log.e(TAG, "Source is being unused because" + source.getClass().getName() +
							" does not extend " + AnimeCatalogueSource.class.getName());
				}
			} catch(Exception e) {
				extension.setError("Failed to get anime sources!", e);
				e.printStackTrace();
			}
		}

		if(mangaExtensionClass != null) {
			try {
				mangaSources = getMangaSources(pkg, mangaExtensionClass, clazzLoader);

				for(var source : mangaSources) {
					extension.isBookExtension = true;
					extension.addProvider(new TachiyomiProvider(source));
				}
			} catch(Exception e) {
				extension.setError("Failed to get manga sources!", e);
				e.printStackTrace();
			}
		}
	}

	@NonNull
	private static List<AnimeSource> getAnimeSources(
			@NonNull PackageInfo pkg,
			@NonNull String animeExtensionClass,
			ClassLoader classLoader
	) {
		var app = pkg.applicationInfo;
		checkSupportedVersionBounds(pkg.versionName, ANIYOMI_EXTENSION_VERSION_MIN, ANIYOMI_EXTENSION_VERSION_MAX);

		var sources = Arrays.stream(animeExtensionClass.split(";"))
				.map(item -> {
					var sourceClass = item.trim();

					if(!sourceClass.startsWith(".")) return sourceClass;
					else return app.packageName + sourceClass;
				})
				.map(item -> {
					try {
						var clazz = Class.forName(item,false, classLoader);
						var constructor = clazz.getConstructor();
						var instance = constructor.newInstance();

						if(instance instanceof AnimeSource source) {
							return List.of(source);
						} else if(instance instanceof AnimeSourceFactory factory) {
							return factory.createSources();
						}

						throw new RuntimeException("Unknown source class type! " + item);
					} catch(ClassNotFoundException e) {
						throw new RuntimeException("Failed to find a class with a requested name! " + item, e);
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
				})
				.collect(Collectors.toList());

		List<AnimeSource> animeSources = new ArrayList<>();

		for(var item : sources) {
			animeSources.addAll(item);
		}

		return animeSources;
	}

	@NonNull
	private static List<MangaSource> getMangaSources(
			@NonNull PackageInfo pkg,
			@NonNull String mangaExtensionClass,
			ClassLoader classLoader
	) {
		var app = pkg.applicationInfo;
		checkSupportedVersionBounds(pkg.versionName, TACHIYOMI_EXTENSION_VERSION_MIN, TACHIYOMI_EXTENSION_VERSION_MAX);

		var sources = Arrays.stream(mangaExtensionClass.split(";"))
				.map(item -> {
					var sourceClass = item.trim();

					if(!sourceClass.startsWith(".")) return sourceClass;
					else return app.packageName + sourceClass;
				})
				.map(item -> {
					try {
						var clazz = Class.forName(item,false, classLoader);
						var constructor = clazz.getConstructor();
						var instance = constructor.newInstance();

						if(instance instanceof MangaSource source) {
							return List.of(source);
						} else if(instance instanceof SourceFactory factory) {
							return factory.createSources();
						}

						throw new RuntimeException("Unknown source class type! " + item);
					} catch(ClassNotFoundException e) {
						throw new RuntimeException("Failed to find a class with a requested name! " + item, e);
					} catch(NoSuchMethodException e) {
						throw new RuntimeException("Failed to get a default constructor!", e);
					} catch(InvocationTargetException e) {
						throw new RuntimeException("Exception was thrown by a constructor!", e);
					} catch(IllegalAccessException e) {
						throw new RuntimeException("Default constructor is inaccessible!", e);
					} catch(InstantiationException e) {
						throw new RuntimeException("Requested class cannot be instanciated!", e);
					}
				})
				.collect(Collectors.toList());

		List<MangaSource> mangaSources = new ArrayList<>();

		for(var item : sources) {
			mangaSources.addAll(item);
		}

		return mangaSources;
	}

	private static void checkSupportedVersionBounds(@NonNull String versionName, double minVersion, double maxVersion) {
		int secondDotIndex = versionName.indexOf(".", versionName.indexOf(".") + 1);

		if(secondDotIndex != -1) {
			versionName = versionName.substring(0, secondDotIndex);
		}

		var version = Double.parseDouble(versionName);

		if(version < minVersion) {
			throw new RuntimeException("Unsupported deprecated version!");
		} else if(version > maxVersion) {
			throw new RuntimeException("Unsupported new version!");
		}
	}

	@NonNull
	public static Collection<Extension> getAllExtensions() {
		return extensions.values();
	}

	public static Collection<Extension> getVideoExtensions(boolean onlyWorking) {
		return extensions.values().stream()
				.filter(Extension::isVideoExtension)
				.collect(Collectors.toList());
	}

	public static Collection<Extension> getVideoExtensions() {
		return getVideoExtensions(true);
	}

	public static Collection<Extension> getBookExtensions() {
		return extensions.values().stream()
				.filter(Extension::isBookExtension)
				.collect(Collectors.toList());
	}

	public static Extension getExtension(String packageName) {
		return extensions.get(packageName);
	}

	@NonNull
	public static Collection<ExtensionsManager> getAllManagers() {
		return Collections.emptyList();
	}
}