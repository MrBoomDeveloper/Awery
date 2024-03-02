package com.mrboomdev.awery;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.mrboomdev.awery.catalog.extensions.ExtensionsFactory;
import com.mrboomdev.awery.catalog.extensions.support.js.JsManager;
import com.mrboomdev.awery.catalog.template.CatalogList;
import com.mrboomdev.awery.data.db.AweryDB;
import com.mrboomdev.awery.data.db.DBCatalogList;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.util.Disposable;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ani.awery.App;
import ani.awery.connections.anilist.Anilist;

@SuppressWarnings("StaticFieldLeak")
public class AweryApp extends App implements Application.ActivityLifecycleCallbacks, Disposable {
	//TODO: Remove these fields after JS extensions will be made
	public static final String ANILIST_EXTENSION_ID = "com.mrboomdev.awery.extension.anilist";
	public static final String ANILIST_CATALOG_ITEM_ID_PREFIX = new JsManager().getId() + ";;;" + ANILIST_EXTENSION_ID + ";;;";
	private static final Map<Class<? extends Activity>, ActivityInfo> activities = new HashMap<>();
	private static final List<Disposable> disposables = new ArrayList<>();
	private static final String TAG = "AweryApp";
	public static final boolean USE_KT_APP_INIT = true;
	private static AweryApp app;
	private static final Handler handler = new Handler(Looper.getMainLooper());
	private static AweryDB db;

	public static void registerDisposable(Disposable disposable) {
		disposables.add(disposable);
	}

	public static AweryDB getDatabase() {
		return db;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A extends Activity> A getActivity(Class<A> clazz) {
		ActivityInfo found = activities.get(clazz);
		if(found != null) return (A) found.activity;

		return null;
	}

	public static void toast(Activity activity, String text, int duration) {
		if(activity == null) {
			Toast.makeText(app, text, duration).show();
			return;
		}

		activity.runOnUiThread(() -> Toast.makeText(activity, text, duration).show());
	}

	public static void toast(String text, int duration) {
		var activity = getAnyActivity();
		toast(activity, text, duration);
	}

	public static void toast(String text) {
		toast(text, 0);
	}

	public static Context getAnyContext() {
		var activity = getAnyActivity();
		if(activity != null) return activity;

		return app;
	}

	public static void setOnBackPressedListener(@NonNull Activity activity, Runnable callback) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, callback::run);
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

	public static void runOnUiThread(Runnable runnable) {
		var activity = getAnyActivity();

		if(activity != null) {
			activity.runOnUiThread(runnable);
		} else {
			handler.post(runnable);
		}
	}

	@Nullable
	@Contract(pure = true)
	public static Activity getAnyActivity() {
		if(activities.size() == 0) return null;
		if(activities.size() == 1) return activities.values().toArray(new ActivityInfo[0])[0].activity;

		var sorted = activities.values()
				.stream()
				.sorted(ActivityInfo::compareTo)
				.toArray();

		return ((ActivityInfo)sorted[0]).activity;
	}

	@Override
	public void onCreate() {
		app = this;
		super.onCreate();

		setupCrashHandler();
		registerActivityLifecycleCallbacks(this);

		ExtensionsFactory.init(this);
		Anilist.INSTANCE.getSavedToken(this);
		db = Room.databaseBuilder(this, AweryDB.class, "db").build();

		var settings = AwerySettings.getInstance(this);
		if(settings.getInt(AwerySettings.LAST_OPENED_VERSION) < 1) {
			new Thread(() -> {
				var lists = List.of(
						DBCatalogList.fromCatalogList(new CatalogList("Currently watching", "1")),
						DBCatalogList.fromCatalogList(new CatalogList("Plan to watch", "2")),
						DBCatalogList.fromCatalogList(new CatalogList("Delayed", "3")),
						DBCatalogList.fromCatalogList(new CatalogList("Completed", "4")),
						DBCatalogList.fromCatalogList(new CatalogList("Dropped", "5")),
						DBCatalogList.fromCatalogList(new CatalogList("Favorites", "6"))
				);

				var array = lists.toArray(new DBCatalogList[0]);
				db.getListDao().insert(array);

				settings.setInt(AwerySettings.LAST_OPENED_VERSION, 1);
				settings.saveSync();
			}).start();
		}
	}

	public static Activity getActivity(@NonNull View view) {
		return getActivity(view.getContext());
	}

	public static int getOrientation() {
		return Resources.getSystem().getConfiguration().orientation;
	}

	public static Configuration getConfiguration() {
		return getAnyContext().getResources().getConfiguration();
	}

	@Nullable
	public static Activity getActivity(Context context) {
		Context ctx = context;

		while(ctx instanceof ContextWrapper wrapper) {
			if(ctx instanceof Activity activity) {
				return activity;
			}

			ctx = wrapper.getBaseContext();
		}

		return null;
	}

	private void setupCrashHandler() {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			if(thread != Looper.getMainLooper().getThread()) {
				Log.e(TAG, "THREAD WAS KILLED! [ Thread name: "
						+ thread.getName() + ", Thread id: "
						+ thread.getId() + " ]", throwable);

				var activity = getAnyActivity();

				if(activity != null) {
					toast(activity, "Unexpected error has happened!", Toast.LENGTH_LONG);
				}

				return;
			}

			Log.e(TAG, "APP JUST CRASHED! [ Thread name: "
					+ thread.getName() + ", Thread id: "
					+ thread.getId() + " ]", throwable);

			var activity = getAnyActivity();

			if(activity != null) {
				toast(activity, "App just crashed :(", Toast.LENGTH_LONG);
				activity.finishAffinity();
			} else {
				System.exit(0);
			}
		});
	}

	@Override
	public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
		var clazz = activity.getClass();
		var info = activities.get(clazz);

		if(info == null) {
			info = new ActivityInfo();
		}

		info.isStopped = false;
		info.isDestroyed = false;

		info.activity = activity;
		activities.put(activity.getClass(), info);
	}

	@Override
	public void onActivityStarted(@NonNull Activity activity) {
		var info = activities.get(activity.getClass());
		if(info == null) return;

		info.isStopped = false;
		info.isDestroyed = false;
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
		info.isDestroyed = true;

		activities.remove(activity.getClass());

		if(activities.size() == 0) {
			runDelayed(() -> {
				if(activities.size() > 0) return;
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
		public Activity activity;
		public boolean isStopped, isDestroyed;


		@Override
		public int compareTo(ActivityInfo o) {
			if(this.isStopped && !o.isStopped) return 1;
			if(!this.isStopped && o.isStopped) return -1;
			if(this.isDestroyed && !o.isDestroyed) return 1;
			if(!this.isDestroyed && o.isDestroyed) return -1;

			return 0;
		}
	}
}