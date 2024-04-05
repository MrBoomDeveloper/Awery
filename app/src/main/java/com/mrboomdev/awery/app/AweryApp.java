package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.data.db.AweryDB;
import com.mrboomdev.awery.data.db.DBCatalogList;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.js.JsManager;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.util.Disposable;
import com.mrboomdev.awery.util.ui.ViewUtil;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;
import okhttp3.OkHttpClient;

@SuppressWarnings("StaticFieldLeak")
public class AweryApp extends Application implements Application.ActivityLifecycleCallbacks, Disposable {
	public static final String CATALOG_LIST_BLACKLIST = "7";
	public static final String CATALOG_LIST_HISTORY = "9";
	public static final List<String> HIDDEN_LISTS = List.of(CATALOG_LIST_BLACKLIST, CATALOG_LIST_HISTORY);
	//TODO: Remove these fields after JS extensions will be made
	public static final String ANILIST_EXTENSION_ID = "com.mrboomdev.awery.extension.anilist";
	public static final String ANILIST_CATALOG_ITEM_ID_PREFIX = new JsManager().getId() + ";;;" + ANILIST_EXTENSION_ID + ";;;";
	private static final WeakHashMap<Runnable, Object> backPressedCallbacks = new WeakHashMap<>();
	private static final Map<Class<? extends AppCompatActivity>, ActivityInfo> activities = new HashMap<>();
	private static final Handler handler = new Handler(Looper.getMainLooper());
	private static final List<Disposable> disposables = new ArrayList<>();
	private static final String TAG = "AweryApp";
	private static Thread mainThread;
	private static AweryApp app;
	private static AweryDB db;

	public static void registerDisposable(Disposable disposable) {
		disposables.add(disposable);
	}

	public static AweryDB getDatabase() {
		return db;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A extends AppCompatActivity> A getActivity(Class<A> clazz) {
		ActivityInfo found = activities.get(clazz);
		if(found != null) return (A) found.activity;

		return null;
	}

	public static Context getContext() {
		return app;
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

	public static Context getAnyContext() {
		Activity activity;

		try {
			activity = getAnyActivity();
			if(activity != null) return activity;
		} catch(IndexOutOfBoundsException ignored) {}

		if(app == null) {
			return getContextUsingPrivateApi();
		}

		return app;
	}

	@Nullable
	@SuppressLint({"PrivateApi","DiscouragedPrivateApi" })
	private static Context getContextUsingPrivateApi() {
		Context context = null;

		try {
			var activityThreadClass = Class.forName("android.app.ActivityThread");
			var method = activityThreadClass.getDeclaredMethod("currentApplication");
			context = (Application) method.invoke(null);
		} catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			Log.e(TAG, "Failed to get Application from ActivityThread!", e);
		}

		if(context == null) {
			try {
				var appGlobalsClass = Class.forName("android.app.AppGlobals");
				var method = appGlobalsClass.getDeclaredMethod("getInitialApplication");
				context = (Application) method.invoke(null);
			} catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				Log.e(TAG, "Failed to get Application from AppGlobals!", e);
			}
		}

		if(context != null) {
			Log.w(TAG, "Using Context from a static method!");
		}

		return context;
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

	public static Context getContext(@NonNull ViewBinding binding) {
		return binding.getRoot().getContext();
	}

	public static Context getContext(@NonNull View view) {
		return view.getContext();
	}

	public static Context getContext(@NonNull LayoutInflater inflater) {
		return inflater.getContext();
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
		var typed = new TypedValue();
		context.getTheme().resolveAttribute(res, typed, true);
		return ContextCompat.getColor(context, typed.resourceId);
	}

	public static boolean isTv() {
		var pm = getAnyContext().getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
	}

	public static void runOnUiThread(Runnable runnable) {
		if(Thread.currentThread() != mainThread) handler.post(runnable);
		else runnable.run();
	}

	@Nullable
	@Contract(pure = true)
	public static AppCompatActivity getAnyActivity() {
		if(activities.isEmpty()) return null;
		if(activities.size() == 1) return activities.values().toArray(new ActivityInfo[0])[0].activity;

		return stream(activities.values())
				.sorted(ActivityInfo::compareTo)
				.findFirst().get().activity;
	}

	/**
	 * Fuck you, Android. It's not my problem that some people do install A LOT of extensions,
	 * so that app stops responding.
	 */
	private static void setupStrictMode() {
		var threadPolicy = new StrictMode.ThreadPolicy.Builder()
				.detectNetwork()
				.detectCustomSlowCalls();

		if(BuildConfig.DEBUG) threadPolicy.penaltyDialog();
		else threadPolicy.penaltyLog();

		StrictMode.setThreadPolicy(threadPolicy.build());

		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectActivityLeaks()
				.penaltyLog()
				.build());
	}

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());

		app = this;
		mainThread = Thread.currentThread();

		var isDarkModeEnabled = AwerySettings.getInstance(this)
				.getBoolean(AwerySettings.DARK_THEME, true);

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
			var logFile = new File(getExternalFilesDir(null), "okttp3_log.txt");
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
		registerActivityLifecycleCallbacks(this);
		ExtensionsFactory.init(this);

		db = Room.databaseBuilder(this, AweryDB.class, "db")
				.addMigrations(AweryDB.MIGRATION_2_3)
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

	public static void restartApp() {
		var context = getAnyContext();
		var pm = context.getPackageManager();

		var intent = pm.getLaunchIntentForPackage(context.getPackageName());
		var component = Objects.requireNonNull(intent).getComponent();

		var mainIntent = Intent.makeRestartActivityTask(component);
		mainIntent.setPackage(context.getPackageName());
		context.startActivity(mainIntent);

		Runtime.getRuntime().exit(0);
	}

	@Nullable
	public static AppCompatActivity getActivity(Context context) {
		while(context instanceof ContextWrapper wrapper) {
			if(context instanceof AppCompatActivity activity) {
				return activity;
			}

			context = wrapper.getBaseContext();
		}

		return null;
	}

	@Override
	public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
		if(activity instanceof AppCompatActivity appCompatActivity) {
			var clazz = activity.getClass();
			var info = activities.get(clazz);

			if(info == null) {
				info = new ActivityInfo();
			}

			info.isStopped = false;
			info.activity = appCompatActivity;
			activities.put(appCompatActivity.getClass(), info);
		} else {
			Log.e(TAG, "Activity is not an AppCompatActivity!");
		}
	}

	@Override
	public void onActivityStarted(@NonNull Activity activity) {
		var info = activities.get(activity.getClass());
		if(info == null) return;

		info.isStopped = false;
	}

	@Override
	public void onActivityResumed(@NonNull Activity activity) {}

	@Override
	public void onActivityPaused(@NonNull Activity activity) {}

	@Override
	public void onActivityStopped(@NonNull Activity activity) {
		var info = activities.get(activity.getClass());
		if(info == null) return;

		info.isStopped = true;
	}

	@Override
	public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

	@Override
	public void onActivityDestroyed(@NonNull Activity activity) {
		var info = activities.get(activity.getClass());
		if(info == null) return;

		info.isStopped = true;
		activities.remove(activity.getClass());

		if(activities.isEmpty()) {
			runDelayed(() -> {
				if(!activities.isEmpty()) return;
				dispose();
			}, 1000);
		}
	}

	public static void cancelDelayed(Runnable runnable) {
		handler.removeCallbacks(runnable);
	}

	public static void runDelayed(Runnable runnable, long delay) {
		handler.postDelayed(runnable, delay);
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

	@Override
	public void dispose() {
		activities.clear();
		app = null;

		for(var disposable : disposables) {
			disposable.dispose();
		}

		disposables.clear();
	}

	private static class ActivityInfo implements Comparable<ActivityInfo> {
		public AppCompatActivity activity;
		public boolean isStopped;

		@Override
		public int compareTo(ActivityInfo o) {
			if(activity.isDestroyed() && !o.activity.isDestroyed()) return 1;
			if(!activity.isDestroyed() && o.activity.isDestroyed()) return -1;
			if(this.isStopped && !o.isStopped) return 1;
			if(!this.isStopped && o.isStopped) return -1;

			return 0;
		}
	}
}