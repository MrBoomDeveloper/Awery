package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AweryLifecycle {
	private static final String TAG = "AweryLifecycle";
	private static AweryApp app;
	private static Thread mainThread;
	private static Handler handler;

	protected static void init(AweryApp app) {
		AweryLifecycle.app = app;

		mainThread = Looper.getMainLooper().getThread();
		handler = new Handler(Looper.getMainLooper());
	}

	public static void restartApp() {
		var context = getAnyContext();
		var pm = context.getPackageManager();

		var intent = pm.getLaunchIntentForPackage(context.getPackageName());
		var component = Objects.requireNonNull(intent).getComponent();

		var mainIntent = Intent.makeRestartActivityTask(component);
		mainIntent.setPackage(context.getPackageName());
		context.startActivity(mainIntent);

		app = null;
		Runtime.getRuntime().exit(0);
	}

	public static void exitApp() {
		var activity = getAnyActivity(Activity.class);
		app = null;

		if(activity != null) activity.finishAffinity();
		else Runtime.getRuntime().exit(0);
	}

	@Nullable
	public static Activity getActivity(Context context) {
		while(context instanceof ContextWrapper wrapper) {
			if(context instanceof Activity activity) {
				return activity;
			}

			context = wrapper.getBaseContext();
		}

		return null;
	}

	@Nullable
	@Contract(pure = true)
	public static <A extends Activity> A getAnyActivity(Class<A> requiredSuper) {
		try {
			var activities = getAllActivitiesRecursively(requiredSuper);
			if(activities.size() == 1) return activities.get(0).activity;

			// App should handle this behaviour properly or else crashes will occur
			if(activities.isEmpty()) return null;

			return stream(activities)
					.sorted(Collections.reverseOrder())
					.findFirst().get().activity;
		} catch(Exception e) {
			Log.e(TAG, "Failed to get any activity!", e);
			toast(getAppContext(), "So your device is not supported :(", 1);
			System.exit(0);
			return null;
		}
	}

	@NonNull
	@SuppressWarnings({"PrivateApi", "unchecked"})
	private static <A extends Activity> List<ActivityInfo<A>> getAllActivitiesRecursively(Class<A> requiredSuper) throws NoSuchFieldException, IllegalAccessException {
		var list = new ArrayList<ActivityInfo<A>>();

		var activityThread = getActivityThread();
		if(activityThread == null) return list;

		var activitiesField = activityThread.getClass().getDeclaredField("mActivities");
		activitiesField.setAccessible(true);
		var activities = activitiesField.get(activityThread);

		if(activities instanceof Map<?, ?> map) {
			for(var record : map.values()) {
				var recordClass = record.getClass();
				var activityField = recordClass.getDeclaredField("activity");
				activityField.setAccessible(true);

				var activity = (Activity) activityField.get(record);

				if(activity == null || !requiredSuper.isInstance(activity)) {
					continue;
				}

				var info = new ActivityInfo<A>();
				info.activity = (A) activity;
				list.add(info);

				var pausedField = recordClass.getDeclaredField("paused");
				pausedField.setAccessible(true);
				info.isPaused = Objects.requireNonNullElse((Boolean) pausedField.get(record), false);

				list.add(info);
			}
		}

		return list;
	}

	@Nullable
	@SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
	private static Object getActivityThread() {
		try {
			var clazz = Class.forName("android.app.ActivityThread");
			var field = clazz.getDeclaredField("sCurrentActivityThread");
			field.setAccessible(true);
			var value = field.get(null);
			if(value != null) return value;
		} catch(Exception ignored) {}

		try {
			var clazz = Class.forName("android.app.AppGlobals");
			var field = clazz.getDeclaredField("sCurrentActivityThread");
			field.setAccessible(true);
			var value = field.get(null);
			if(value != null) return value;
		} catch(Exception ignored) {}

		try {
			var clazz = Class.forName("android.app.ActivityThread");
			var method = clazz.getDeclaredMethod("currentActivityThread");
			method.setAccessible(true);
			return method.invoke(null);
		} catch(Exception ignored) {
			return null;
		}
	}

	public static Runnable runOnUiThread(Runnable runnable) {
		if(Thread.currentThread() != mainThread) handler.post(runnable);
		else runnable.run();

		return runnable;
	}

	/**
	 * Runs the callback on the main thread and checks whatever RecyclerView is computing layout or not to avoid exceptions.
	 * @param callback Will be ran on the main thread if RecyclerView isn't computing layout
	 * @param recycler Target RecyclerView
	 * @return May be a different callback depending on the state of the RecyclerView, so that you can cancel it.
	 */
	@NonNull
	public static Runnable runOnUiThread(Runnable callback, RecyclerView recycler) {
		if(Thread.currentThread() != mainThread || recycler.isComputingLayout()) {
			Runnable runnable = () -> runOnUiThread(callback, recycler);
			handler.post(runnable);
			return runnable;
		}

		callback.run();
		return callback;
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

	public static Context getAnyContext() {
		Activity activity;

		try {
			activity = getAnyActivity(Activity.class);
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

	public static Context getAppContext() {
		return app;
	}

	public static void cancelDelayed(Runnable runnable) {
		handler.removeCallbacks(runnable);
	}

	public static void runDelayed(Runnable runnable, long delay) {
		handler.postDelayed(runnable, delay);
	}

	private static class ActivityInfo<A extends Activity> implements Comparable<ActivityInfo<A>> {
		public A activity;
		public boolean isPaused;

		@Override
		public int compareTo(ActivityInfo o) {
			if(activity.hasWindowFocus() && !o.activity.hasWindowFocus()) return 1;
			if(!activity.hasWindowFocus() && o.activity.hasWindowFocus()) return -1;

			if(isPaused && !o.isPaused) return -1;
			if(!isPaused && o.isPaused) return 1;

			return Integer.compare(activity.getTaskId(), o.activity.getTaskId());
		}
	}
}