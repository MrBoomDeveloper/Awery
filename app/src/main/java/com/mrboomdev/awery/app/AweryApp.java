package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;

import android.app.Activity;
import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.StrictMode;
import android.os.strictmode.InstanceCountViolation;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.AweryDB;
import com.mrboomdev.awery.data.db.DBCatalogList;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.extensions.support.js.JsManager;
import com.mrboomdev.awery.sdk.PlatformApi;
import com.mrboomdev.awery.util.ui.ViewUtil;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;
import okhttp3.OkHttpClient;

@SuppressWarnings("StaticFieldLeak")
public class AweryApp extends Application {
	public static final String CATALOG_LIST_BLACKLIST = "7";
	public static final String CATALOG_LIST_HISTORY = "9";
	public static final List<String> HIDDEN_LISTS = List.of(CATALOG_LIST_BLACKLIST, CATALOG_LIST_HISTORY);
	//TODO: Remove these fields after JS extensions will be made
	public static final String ANILIST_EXTENSION_ID = "com.mrboomdev.awery.extension.anilist";
	public static final String ANILIST_CATALOG_ITEM_ID_PREFIX = new JsManager().getId() + ";;;" + ANILIST_EXTENSION_ID + ";;;";
	private static final WeakHashMap<Runnable, Object> backPressedCallbacks = new WeakHashMap<>();
	private static final String TAG = "AweryApp";
	private static AweryDB db;

	public static AweryDB getDatabase() {
		return db;
	}

	public static void toast(Context context, Object text, int duration) {
		var string = text == null ? "null" : text.toString();
		runOnUiThread(() -> Toast.makeText(context, string, duration).show());
	}

	public static void toast(Object text, int duration) {
		toast(getAnyContext(), text, duration);
	}

	public static void toast(Object text) {
		toast(text, 0);
	}

	public static void toast(@StringRes int res) {
		toast(getAnyContext().getString(res));
	}

	public static void removeOnBackPressedListener(@NonNull Activity activity, Runnable callback) {
		var onBackInvokedCallback = backPressedCallbacks.remove(callback);
		if(onBackInvokedCallback == null) return;

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(
					(OnBackInvokedCallback) onBackInvokedCallback);
		} else {
			if(onBackInvokedCallback instanceof OnBackPressedCallback backPressedCallback) backPressedCallback.remove();
			else throw new IllegalArgumentException("Callback must implement OnBackPressedCallback!");
		}
	}

	public static void addOnBackPressedListener(@NonNull Activity activity, Runnable callback) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			var onBackInvokedCallback = (OnBackInvokedCallback) callback::run;
			backPressedCallbacks.put(callback, onBackInvokedCallback);

			var dispatcher = activity.getOnBackInvokedDispatcher();
			dispatcher.registerOnBackInvokedCallback(0, onBackInvokedCallback);
		} else {
			if(activity instanceof OnBackPressedDispatcherOwner owner) {
				owner.getOnBackPressedDispatcher().addCallback(owner, new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {
						callback.run();
					}
				});
			} else {
				throw new IllegalArgumentException("Activity must implement OnBackPressedDispatcherOwner!");
			}
		}
	}

	@NonNull
	@Contract("_ -> new")
	public static <E> Stream<E> stream(Collection<E> e) {
		return StreamSupport.stream(e);
	}

	@SafeVarargs
	@NonNull
	@Contract("_ -> new")
	public static <E> Stream<E> stream(E... e) {
		return StreamSupport.stream(Arrays.asList(e));
	}

	@NonNull
	@Contract("_ -> new")
	public static <K, V> Stream<Map.Entry<K,V>> stream(@NonNull Map<K, V> map) {
		return StreamSupport.stream(map.entrySet());
	}

	public static int resolveAttrColor(@NonNull Context context, @AttrRes int res) {
		return MaterialColors.getColor(context, res, Color.BLACK);
	}

	@Nullable
	public static String getString(@NonNull Context context, String name) {
		if(name == null) return null;

		try {
			var clazz = R.string.class;
			var field = clazz.getField(name);
			return context.getString(field.getInt(null));
		} catch(NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	public static boolean isLandscape() {
		return getAnyContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static boolean isTv() {
		var pm = getAnyContext().getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
	}

	@NonNull
	@Contract("null -> fail; !null -> param1")
	public static <T> T requireNonNull(T obj) {
		if(obj == null) throw new NullPointerException();
		return obj;
	}

	public static boolean nonNull(Object obj) {
		return obj != null;
	}

	/**
	 * Fuck you, Android. It's not my problem that some people do install A LOT of extensions,
	 * so that app stops responding.
	 */
	private static void setupStrictMode() {
		var threadPolicy = new StrictMode.ThreadPolicy.Builder()
				.detectNetwork();

		var vmPolicy = new StrictMode.VmPolicy.Builder()
				.detectActivityLeaks()
				.penaltyLog();

		if(BuildConfig.DEBUG) threadPolicy.penaltyDialog();
		else threadPolicy.penaltyLog();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			vmPolicy.penaltyListener(Runnable::run, v -> {
				if(BuildConfig.DEBUG) {
					if(v instanceof InstanceCountViolation) return;

					var activity = getAnyActivity(AppCompatActivity.class);
					CrashHandler.showErrorDialog(activity, v);
				}
			});

			threadPolicy.penaltyListener(Runnable::run, v -> {
				if(BuildConfig.DEBUG) {
					var activity = getAnyActivity(AppCompatActivity.class);
					CrashHandler.showErrorDialog(activity, v);
				}
			});
		}

		StrictMode.setThreadPolicy(threadPolicy.build());
		StrictMode.setVmPolicy(vmPolicy.build());
	}

	@Override
	protected void attachBaseContext(@NonNull Context base) {
		super.attachBaseContext(base);
		CrashHandler.setup(this);
	}

	@Override
	public void onCreate() {
		PlatformApi.setInstance(new AweryPlatform());
		AweryLifecycle.init(this);

		var isDarkModeEnabled = AwerySettings.getInstance(this)
				.getBoolean(AwerySettings.theme.DARK_THEME);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			((UiModeManager) getSystemService(UI_MODE_SERVICE))
					.setApplicationNightMode(isDarkModeEnabled
						? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO);
		} else {
			AppCompatDelegate.setDefaultNightMode(isDarkModeEnabled
					? AppCompatDelegate.MODE_NIGHT_YES
					: AppCompatDelegate.MODE_NIGHT_NO);
		}

		super.onCreate();

		if(AwerySettings.getInstance(this).getBoolean(AwerySettings.VERBOSE_NETWORK)) {
			var logFile = new File(getExternalFilesDir(null), "okhttp3_log.txt");
			logFile.delete();

			try {
				logFile.createNewFile();

				Logger.getLogger(OkHttpClient.class.getName()).addHandler(new java.util.logging.Handler() {
					@Override
					public void publish(LogRecord record) {
						try(var writer = new FileWriter(logFile, true)) {
							writer.write("[" + record.getLevel() + "] " + record.getMessage() + "\n");
						} catch(IOException e) {
							Log.e(TAG, "Failed to write log file!", e);
						}
					}

					@Override
					public void flush() {}

					@Override
					public void close() throws SecurityException {}
				});
			} catch(IOException e) {
				Log.e(TAG, "Failed to create log file!", e);
			}
		}

		setupStrictMode();
		ExtensionsFactory.init(this);

		db = Room.databaseBuilder(this, AweryDB.class, "db")
				.addMigrations(AweryDB.MIGRATION_2_3, AweryDB.MIGRATION_3_4)
				.build();

		var settings = AwerySettings.getInstance(this);
		if(settings.getInt(AwerySettings.LAST_OPENED_VERSION) < 1) {
			new Thread(() -> {
				db.getListDao().insert(
						DBCatalogList.fromCatalogList(new CatalogList("Currently watching", "1")),
						DBCatalogList.fromCatalogList(new CatalogList("Plan to watch", "2")),
						DBCatalogList.fromCatalogList(new CatalogList("Delayed", "3")),
						DBCatalogList.fromCatalogList(new CatalogList("Completed", "4")),
						DBCatalogList.fromCatalogList(new CatalogList("Dropped", "5")),
						DBCatalogList.fromCatalogList(new CatalogList("Favorites", "6")),
						DBCatalogList.fromCatalogList(new CatalogList("Hidden", CATALOG_LIST_BLACKLIST)),
						DBCatalogList.fromCatalogList(new CatalogList("History", CATALOG_LIST_HISTORY)));

				settings.setInt(AwerySettings.LAST_OPENED_VERSION, 1);
				settings.saveSync();
			}).start();
		}
	}

	public static int getOrientation() {
		return Resources.getSystem().getConfiguration().orientation;
	}

	public static Configuration getConfiguration(@NonNull Context context) {
		return context.getResources().getConfiguration();
	}

	public static Configuration getConfiguration() {
		return getConfiguration(getAnyContext());
	}

	public static void snackbar(@NonNull Activity activity, Object input) {
		runOnUiThread(() -> {
			var text = input == null ? "null" : input.toString();
			var rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
			var snackbar = Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT);

			ViewUtil.<FrameLayout.LayoutParams>useLayoutParams(snackbar.getView(), params -> {
				params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
				params.width = WRAP_CONTENT;
			});

			snackbar.getView().setOnClickListener(v -> snackbar.dismiss());
			snackbar.show();
		});
	}
}