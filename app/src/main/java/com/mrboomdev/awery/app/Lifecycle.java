package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.invokeMethod;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import static java.util.Objects.requireNonNull;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class Lifecycle implements Application.ActivityLifecycleCallbacks {
	private static final WeakHashMap<Activity, ActivityInfo<Activity>> infos = new WeakHashMap<>();
	private static final String TAG = "Lifecycle";
	private static UniqueIdGenerator activityRequestCodes;
	private static App app;
	private static Handler handler;

	public static int getActivityResultCode() {
		if(activityRequestCodes == null) {
			activityRequestCodes = new UniqueIdGenerator();
			activityRequestCodes.getInteger();
		}

		return activityRequestCodes.getInteger();
	}

	private Lifecycle() {}

	protected static void init(@NonNull App app) {
		Lifecycle.app = app;
		app.registerActivityLifecycleCallbacks(new Lifecycle());
		handler = new Handler(Looper.getMainLooper());
	}

	@NonNull
	public static FragmentManager getFragmentManager(Context context) {
		var activity = getActivity(context);

		if(activity instanceof FragmentActivity fragmentActivity) {
			return fragmentActivity.getSupportFragmentManager();
		}

		throw new UnsupportedOperationException("This activity doesn't extend an FragmentActivity class!");
	}

	public static void restartApp() {
		Log.i(TAG, "restartApp() has been invoked!");

		var context = getAnyContext();
		var pm = context.getPackageManager();

		var intent = pm.getLaunchIntentForPackage(context.getPackageName());
		var component = requireNonNull(intent).getComponent();

		var mainIntent = Intent.makeRestartActivityTask(component);
		mainIntent.setPackage(context.getPackageName());
		context.startActivity(mainIntent);

		app = null;
		System.exit(0);
	}

	public static <T extends Activity> View getAnyRootView(Class<T> clazz) {
		return stream(getActivities(clazz))
				.map(activity -> activity.getWindow().getDecorView())
				.filter(Objects::nonNull)
				.map(View::getRootView)
				.findFirst().orElse(null);
	}

	public static void exitApp() {
		var activity = getAnyActivity(Activity.class);
		app = null;

		if(activity != null) activity.finishAffinity();
		else Runtime.getRuntime().exit(0);
	}

	@SuppressWarnings("unchecked")
	private <A extends Activity> ActivityInfo<A> getActivityInfo(A activity) {
		return (ActivityInfo<A>) infos.computeIfAbsent(activity, ActivityInfo::new);
	}

	@Override
	public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
		var info = infos.get(activity);
		if(info != null) return;

		info = new ActivityInfo<>(activity);
		info.lastActiveTime = info.lastActiveTime == 0 ? System.currentTimeMillis() : 1;
		infos.put(activity, info);
	}

	@Override
	public void onActivityStarted(@NonNull Activity activity) {}

	@Override
	public void onActivityResumed(@NonNull Activity activity) {
		var info = getActivityInfo(activity);
		info.isPaused = false;
		info.lastActiveTime = System.currentTimeMillis();
	}

	@Override
	public void onActivityPaused(@NonNull Activity activity) {
		getActivityInfo(activity).isPaused = true;
	}

	@Override
	public void onActivityStopped(@NonNull Activity activity) {
		getActivityInfo(activity).isPaused = true;
	}

	@Override
	public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

	@Override
	public void onActivityDestroyed(@NonNull Activity activity) {
		getActivityInfo(activity).isPaused = true;
	}

	/**
	 * DO NOT EVER USE DIRECTLY THIS CLASS!
	 * It was made just for the Android Framework to work properly!
	 */
	public static class CallbackFragment extends Fragment {
		private final FragmentManager fragmentManager;
		private final ActivityResultCallback callback;
		private final ActivityPermissionsResultCallback permissionsResultCallback;
		private final int requestCode;

		public CallbackFragment(
				FragmentManager manager,
				ActivityResultCallback callback,
				ActivityPermissionsResultCallback permissionsResultCallback,
				int requestCode
		) {
			this.fragmentManager = manager;
			this.callback = callback;
			this.requestCode = requestCode;
			this.permissionsResultCallback = permissionsResultCallback;
		}

		@Override
		@SuppressWarnings("deprecation")
		public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
			if(requestCode != this.requestCode || callback == null) return;

			callback.run(resultCode, data);
			finish();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
			if(requestCode != this.requestCode || permissionsResultCallback == null) return;

			if(permissions.length == 0) {
				permissionsResultCallback.onPermissionResult(false);
			} else if(permissions.length == 1) {
				permissionsResultCallback.onPermissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
			} else {
				throw new IllegalStateException("Somehow you've requested multiple permissions at once. This behaviour isn't supported.");
			}

			finish();
		}

		private void finish() {
			fragmentManager.beginTransaction().remove(this).commit();
			fragmentManager.executePendingTransactions();
		}
	}

	public interface ActivityPermissionsResultCallback {
		void onPermissionResult(boolean didGranted);
	}

	public interface ActivityResultCallback {
		void run(int resultCode, Intent data);
	}

	@NonNull
	@MainThread
	private static Fragment addActivityResultListener(
			Activity activity,
			int requestCode,
			ActivityResultCallback callback,
			ActivityPermissionsResultCallback permissionsResultCallback
	) {
		if(activity instanceof FragmentActivity fragmentActivity) {
			var fragmentManager = fragmentActivity.getSupportFragmentManager();
			var fragment = new CallbackFragment(fragmentManager, callback, permissionsResultCallback, requestCode);

			fragmentManager.beginTransaction().add(fragment, null).commit();
			fragmentManager.executePendingTransactions();

			return fragment;
		} else {
			throw new IllegalArgumentException("Activity must be an instance of FragmentActivity!");
		}
	}

	/**
	 * This method is a little bit hacky so after library update it may break.
	 * @param context Context from the {@link FragmentActivity}
	 * @author MrBoomDev
	 */
	@SuppressWarnings("deprecation")
	@MainThread
	public static void startActivityForResult(
			Context context,
			Intent intent,
			int requestCode,
			ActivityResultCallback callback
	) {
		addActivityResultListener(getActivity(context), requestCode, callback, null)
				.startActivityForResult(intent, requestCode);
	}

	/**
	 * This method is a little bit hacky so after library update it may break.
	 * @param context Context from the {@link FragmentActivity}
	 * @author MrBoomDev
	 */
	@MainThread
	public static void startActivityForResult(
			Context context,
			Intent intent,
			ActivityResultCallback callback
	) {
		startActivityForResult(context, intent, getActivityResultCode(), callback);
	}

	public static void requestPermission(
			Context context,
			@NonNull Permission permission,
			int requestCode,
			ActivityPermissionsResultCallback callback
	) {
		var constant = permission.getManifestConstants();

		if(constant == null || ContextCompat.checkSelfPermission(context, constant) == PackageManager.PERMISSION_GRANTED) {
			callback.onPermissionResult(true);
			return;
		}

		var activity = requireNonNull(getActivity(context));
		addActivityResultListener(activity, requestCode, null, callback);
		ActivityCompat.requestPermissions(activity, new String[] { constant }, requestCode);
	}

	@Nullable
	public static Activity getActivity(Context context) {
		if(context instanceof Activity activity) {
			return activity;
		}

		if(context instanceof ContextWrapper wrapper) {
			return getActivity(wrapper.getBaseContext());
		}

		return null;
	}

	public static <A extends Activity> List<A> getActivities(Class<A> requiredSuper) {
		try {
			return stream(getAllActivitiesRecursively(requiredSuper))
					.sorted(Collections.reverseOrder())
					.map(info -> info.activity)
					.toList();
		} catch(Exception e) {
			Log.e(TAG, "Failed to get activities!", e);
			toast("Your device is not supported :(", 1);
			System.exit(0);
			return null;
		}
	}

	@NotNull
	@Contract(pure = true)
	public static <A extends Activity> A requireAnyActivity(Class<A> requiredSuper) {
		return requireNonNull(getAnyActivity(requiredSuper));
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
			toast("So your device is not supported :(", 1);
			System.exit(0);
			return null;
		}
	}

	private static Field activitiesField, pausedField, activityField;

	@NonNull
	@SuppressWarnings({"PrivateApi", "unchecked"})
	private static <A extends Activity> List<ActivityInfo<A>> getAllActivitiesRecursively(Class<A> requiredSuper) throws NoSuchFieldException, IllegalAccessException {
		var list = new ArrayList<ActivityInfo<A>>();

		var activityThread = getActivityThread();
		if(activityThread == null) return list;

		if(activitiesField == null) {
			activitiesField = activityThread.getClass().getDeclaredField("mActivities");
			activitiesField.setAccessible(true);
		}

		var activities = activitiesField.get(activityThread);

		if(activities instanceof Map<?, ?> map) {
			for(var record : map.values()) {
				if(activityField == null) {
					activityField = record.getClass().getDeclaredField("activity");
					activityField.setAccessible(true);
				}

				var activity = (Activity) activityField.get(record);

				if(activity == null || !requiredSuper.isInstance(activity)) {
					continue;
				}

				var info = new ActivityInfo<>((A) activity);
				list.add(info);

				if(pausedField == null) {
					pausedField = record.getClass().getDeclaredField("paused");
					pausedField.setAccessible(true);
				}

				info.isPaused = Objects.requireNonNullElse((Boolean) pausedField.get(record), false);

				list.add(info);
			}
		}

		return list;
	}

	private static Field activityThreadField;
	private static Method activityThreadMethod;

	@Nullable
	@SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
	private static Object getActivityThread() {
		if(activityThreadField != null) {
			try {
				return activityThreadField.get(null);
			} catch(IllegalAccessException ignored) {}
		}

		if(activityThreadMethod != null) {
			try {
				return activityThreadMethod.invoke(null);
			} catch(IllegalAccessException | InvocationTargetException ignored) {}
		}

		if(activityThreadField == null) {
			try {
				var clazz = Class.forName("android.app.ActivityThread");
				activityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
				activityThreadField.setAccessible(true);
				var value = activityThreadField.get(null);
				if(value != null) return value;
			} catch(IllegalAccessException | ClassNotFoundException | NoSuchFieldException ignored) {}

			try {
				var clazz = Class.forName("android.app.AppGlobals");
				activityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
				activityThreadField.setAccessible(true);
				var value = activityThreadField.get(null);
				if(value != null) return value;
			} catch(IllegalAccessException | ClassNotFoundException | NoSuchFieldException ignored) {}
		}

		if(activityThreadMethod == null) {
			try {
				var clazz = Class.forName("android.app.ActivityThread");
				activityThreadMethod = clazz.getDeclaredMethod("currentActivityThread");
				activityThreadMethod.setAccessible(true);
				return activityThreadMethod.invoke(null);
			} catch(IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException ignored) {}
		}

		throw new UnsupportedOperationException("Your device is unsupported!");
	}

	@Nullable
	public static Runnable postRunnable(Runnable runnable) {
		return handler.post(runnable) ? runnable : null;
	}

	public static Runnable runOnUiThread(Runnable runnable) {
		if(!isMainThread()) handler.post(runnable);
		else runnable.run();

		return runnable;
	}

	public static boolean isMainThread() {
		return Looper.getMainLooper() == Looper.myLooper();
	}

	/**
	 * Runs the callback on the main thread and checks whatever RecyclerView is computing layout or not to avoid exceptions.
	 * @param callback Will be ran on the main thread if RecyclerView isn't computing layout
	 * @param recycler Target RecyclerView
	 * @return May be a different callback depending on the state of the RecyclerView, so that you can cancel it.
	 */
	@NonNull
	public static Runnable runOnUiThread(Runnable callback, RecyclerView recycler) {
		if(!isMainThread() || recycler.isComputingLayout()) {
			Runnable runnable = () -> runOnUiThread(callback, recycler);
			handler.post(runnable);
			return runnable;
		}

		callback.run();
		return callback;
	}

	public static Context getContext(ViewBinding binding) {
		if(binding == null) return null;
		return binding.getRoot().getContext();
	}

	@Contract("null -> null")
	public static Context getContext(View view) {
		if(view == null) return null;
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

		return getAppContext();
	}

	@Nullable
	@SuppressLint({"PrivateApi", "DiscouragedPrivateApi" })
	private static App getAppUsingPrivateApi() {
		var context = (App) invokeMethod(
				"android.app.ActivityThread",
				"currentApplication");

		if(context != null) return context;

		context = (App) invokeMethod(
				"android.app.AppGlobals",
				"getInitialApplication");

		return context;
	}

	public static Application getAppContext() {
		if(app != null) {
			return app;
		}

		app = getAppUsingPrivateApi();

		if(app == null) {
			var activity = getAnyActivity(Activity.class);

			if(activity != null) {
				app = (App) activity.getApplicationContext();
			}
		}

		return app;
	}

	public static void cancelDelayed(Runnable runnable) {
		handler.removeCallbacks(runnable);
	}

	public static void runDelayed(Runnable runnable, long delay) {
		handler.postDelayed(runnable, delay);
	}

	@NonNull
	public static Runnable runDelayed(Runnable runnable, long delay, RecyclerView recycler) {
		Runnable result = () -> runOnUiThread(runnable, recycler);
		handler.postDelayed(result, delay);
		return result;
	}

	private static class ActivityInfo<A extends Activity> implements Comparable<ActivityInfo<A>> {
		public long lastActiveTime;
		public final A activity;
		public boolean isPaused;

		public ActivityInfo(A activity) {
			this.activity = activity;
		}

		@Override
		public int compareTo(@NonNull ActivityInfo o) {
			var realMe = infos.get(activity);
			var realHe = infos.get(o.activity);

			if(realMe != null) {
				isPaused = realMe.isPaused;
				lastActiveTime = realMe.lastActiveTime;
			}

			if(realHe != null) {
				o.isPaused = realHe.isPaused;
				o.lastActiveTime = realHe.lastActiveTime;
			}

			if(lastActiveTime != 0 && o.lastActiveTime != 0) {
				if(lastActiveTime > o.lastActiveTime) return 1;
				if(lastActiveTime < o.lastActiveTime) return -1;
			}

			if(isPaused && !o.isPaused) return -1;
			if(!isPaused && o.isPaused) return 1;

			if(activity.isDestroyed() && !o.activity.isDestroyed()) return -1;
			if(!activity.isDestroyed() && o.activity.isDestroyed()) return 1;

			if(hasWindowFocus(activity) && !hasWindowFocus(o.activity)) return 1;
			if(!hasWindowFocus(activity) && hasWindowFocus(o.activity)) return -1;

			return Integer.compare(activity.getTaskId(), o.activity.getTaskId());
		}

		/**
		 * Sometimes Android do throw this exception "java.lang.RuntimeException: Window couldn't find content container view".
		 * Because we just need to check if the activity has focus or not we ignore the exception and return false.
		 * @return true if the activity has focus or false if it doesn't
		 * @author MrBoomDev
		 */
		private boolean hasWindowFocus(Activity activity) {
			try {
				return activity.hasWindowFocus();
			} catch(RuntimeException e) {
				return false;
			}
		}
	}

	public enum Permission {
		NOTIFICATIONS(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.POST_NOTIFICATIONS : null),
		STORAGE(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? Manifest.permission.MANAGE_EXTERNAL_STORAGE : Manifest.permission.WRITE_EXTERNAL_STORAGE);

		private final String manifestConst;

		Permission(String manifestConst) {
			this.manifestConst = manifestConst;
		}

		@Nullable
		public String getManifestConstants() {
			return manifestConst;
		}
	}
}