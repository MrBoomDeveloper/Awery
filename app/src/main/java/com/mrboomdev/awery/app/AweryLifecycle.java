package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.mrboomdev.awery.util.Disposable;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AweryLifecycle implements Application.ActivityLifecycleCallbacks {
	private static final String TAG = "AweryLifecycle";
	protected static final Map<Class<? extends AppCompatActivity>, ActivityInfo> activities = new HashMap<>();
	protected static final List<Disposable> disposables = new ArrayList<>();
	protected static AweryApp app;
	private static final Thread mainThread = Looper.getMainLooper().getThread();
	private static final Handler handler = new Handler(Looper.getMainLooper());

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A extends AppCompatActivity> A getActivity(Class<A> clazz) {
		var found = activities.get(clazz);
		if(found != null) return (A) found.activity;

		return null;
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

	@Nullable
	@Contract(pure = true)
	public static AppCompatActivity getAnyActivity() {
		if(activities.isEmpty()) return null;
		if(activities.size() == 1) return activities.values().toArray(new ActivityInfo[0])[0].activity;

		return stream(activities.values())
				.sorted(ActivityInfo::compareTo)
				.findFirst().get().activity;
	}

	public static void runOnUiThread(Runnable runnable) {
		if(Thread.currentThread() != mainThread) handler.post(runnable);
		else runnable.run();
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

	public static Context getAppContext() {
		return app;
	}

	public static void registerDisposable(Disposable disposable) {
		disposables.add(disposable);
	}

	public static void dispose() {
		activities.clear();
		app = null;

		for(var disposable : disposables) {
			disposable.dispose();
		}

		disposables.clear();
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
	public void onActivityResumed(@NonNull Activity activity) {

	}

	@Override
	public void onActivityPaused(@NonNull Activity activity) {

	}

	@Override
	public void onActivityStopped(@NonNull Activity activity) {
		var info = activities.get(activity.getClass());
		if(info == null) return;

		info.isStopped = true;
	}

	public static void cancelDelayed(Runnable runnable) {
		handler.removeCallbacks(runnable);
	}

	public static void runDelayed(Runnable runnable, long delay) {
		handler.postDelayed(runnable, delay);
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
				AweryLifecycle.dispose();
			}, 1000);
		}
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