package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.getAppContext;
import static com.mrboomdev.awery.app.AweryLifecycle.postRunnable;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.CATALOG_LIST_BLACKLIST;
import static com.mrboomdev.awery.data.Constants.CATALOG_LIST_HISTORY;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.os.strictmode.InstanceCountViolation;
import android.util.Log;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.AweryDB;
import com.mrboomdev.awery.data.db.item.DBCatalogList;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.PlatformApi;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.SpoilerPlugin;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.noties.markwon.Markwon;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import okhttp3.OkHttpClient;

@SuppressWarnings("StaticFieldLeak")
public class AweryApp extends Application {
	private static final WeakHashMap<Runnable, Object> backPressedCallbacks = new WeakHashMap<>();
	private static final String TAG = "AweryApp";
	private static AweryDB db;

	@NonNull
	public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
		var bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		var config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		bitmap.setConfig(config);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	@NonNull
	public static Markwon getMarkwon(Context context) {
		return Markwon.builder(context)
				.usePlugin(new SoftBreakAddsNewLinePlugin())
				.usePlugin(StrikethroughPlugin.create())
				.usePlugin(GlideImagesPlugin.create(context))
				.usePlugin(LinkifyPlugin.create())
				.usePlugin(SpoilerPlugin.create())
				.usePlugin(HtmlPlugin.create())
				.build();
	}

	public static void copyToClipboard(String label, @NonNull String content) {
		copyToClipboard(ClipData.newPlainText(label, content));
	}

	public static void copyToClipboard(String label, @NonNull Uri content) {
		copyToClipboard(ClipData.newRawUri(label, content));
	}

	public static void copyToClipboard(ClipData clipData) {
		var clipboard = getAnyContext().getSystemService(ClipboardManager.class);
		clipboard.setPrimaryClip(clipData);

		// Android 13 and higher shows a visual confirmation of copied contents
		// https://developer.android.com/about/versions/13/features/copy-paste
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
			toast(R.string.copied_to_clipboard);
		}
	}

	/**
	 * A hacky method to fix the height, width of the dialog and color of the navigation bar.
	 * @author MrBoomDev
	 */
	public static void fixDialog(@NonNull Dialog dialog) {
		var context = dialog.getContext();
		var window = dialog.getWindow();

		if(window == null) {
			throw new IllegalStateException("You can't invoke this method before dialog is being shown!");
		}

		if(dialog instanceof BottomSheetDialog sheet) {
			sheet.getBehavior().setPeekHeight(1000);
			window.setNavigationBarColor(SurfaceColors.SURFACE_1.getColor(context));
		}

		if(dialog instanceof SideSheetDialog) {
			var sheet = window.findViewById(com.google.android.material.R.id.m3_side_sheet);
			useLayoutParams(sheet, params -> params.width = dpPx(400));
			window.setNavigationBarColor(SurfaceColors.SURFACE_1.getColor(context));
		} else {
			/* If we'll try to do this shit with the SideSheetDialog, it will get centered,
			   so we use different approaches for different dialog types.*/

			if(getConfiguration().screenWidthDp > 400) {
				window.setLayout(dpPx(400), MATCH_PARENT);
			}
		}
	}

	public static int getResourceId(@NonNull Class<?> type, String res) {
		if(res == null) return 0;

		try {
			var field = type.getDeclaredField(res);
			field.setAccessible(true);
			var result = field.get(null);

			if(result == null) {
				Log.e(TAG, "Resource id \"" + res + "\" was not initialized in \"" + type.getName() + "\"!");
				return 0;
			}

			return (int) result;
		} catch(NoSuchFieldException e) {
			return 0;
		} catch(IllegalAccessException e) {
			throw new IllegalStateException(
					"Generated resource id filed cannot be private! Check if the provided class is the R class", e);
		}
	}

	/**
	 * @return An resource id or 0 if resource was not found.
	 * @author MrBoomDev
	 */
	@Nullable
	public static String getString(Class<?> clazz, String string) {
		var id = getResourceId(clazz, string);
		if(id == 0) return null;

		return getAnyContext().getString(id);
	}

	public static AweryDB getDatabase() {
		if(db != null) {
			return db;
		}

		synchronized(AweryApp.class) {
			if(db != null) {
				return db;
			}

			db = Room.databaseBuilder(getAppContext(), AweryDB.class, "db")
					.addMigrations(AweryDB.MIGRATION_2_3, AweryDB.MIGRATION_3_4)
					.build();
		}

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

	public static void toast(@StringRes int res, int duration) {
		toast(getAnyContext().getString(res), duration);
	}

	/**
	 * Safely enables the "Edge to edge" experience.
	 * I really don't know why, but sometimes it just randomly crashes!
	 * Because of it we have to rerun this method on a next frame.
	 * @author MrBoomDev
	 */
	public static void enableEdgeToEdge(ComponentActivity context) {
		try {
			EdgeToEdge.enable(context);
		} catch(RuntimeException e) {
			Log.e(TAG, "Failed to enable EdgeToEdge! Will retry a little bit later.", e);
			postRunnable(() -> enableEdgeToEdge(context));
		}
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

	public static int resolveAttrColor(@NonNull Context context, @AttrRes int res) {
		return MaterialColors.getColor(context, res, Color.BLACK);
	}

	public static boolean isLandscape() {
		return getAnyContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static boolean isTv() {
		var pm = getAnyContext().getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
	}

	public static void openUrl(String url) {
		try {
			var intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			getAnyContext().startActivity(intent);
		} catch(ActivityNotFoundException e) {
			Log.e(TAG, "Cannot open url!", e);

			new DialogBuilder(getAnyActivity(AppCompatActivity.class))
					.setTitle("Here's the link")
					.setMessage(url)
					.setPositiveButton(R.string.ok, DialogBuilder::dismiss)
					.show();
		}
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
		AweryNotifications.registerNotificationChannels(this);
		PlatformApi.setInstance(new AweryPlatform());
		AweryLifecycle.init(this);
		ThemeManager.applyApp(this);
		super.onCreate();

		if(AwerySettings.LOG_NETWORK.getValue()) {
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

		if(AwerySettings.LAST_OPENED_VERSION.getValue() < 1) {
			new Thread(() -> {
				getDatabase().getListDao().insert(
						DBCatalogList.fromCatalogList(new CatalogList(getString(R.string.currently_watching), "1")),
						DBCatalogList.fromCatalogList(new CatalogList(getString(R.string.planning_watch), "2")),
						DBCatalogList.fromCatalogList(new CatalogList(getString(R.string.delayed), "3")),
						DBCatalogList.fromCatalogList(new CatalogList(getString(R.string.completed), "4")),
						DBCatalogList.fromCatalogList(new CatalogList(getString(R.string.dropped), "5")),
						DBCatalogList.fromCatalogList(new CatalogList(getString(R.string.favourites), "6")),
						DBCatalogList.fromCatalogList(new CatalogList("Hidden", CATALOG_LIST_BLACKLIST)),
						DBCatalogList.fromCatalogList(new CatalogList("History", CATALOG_LIST_HISTORY)));

				getPrefs().setValue(AwerySettings.LAST_OPENED_VERSION, 1).saveSync();
			}).start();
		}
	}

	public static int getOrientation() {
		return Resources.getSystem().getConfiguration().orientation;
	}

	public static AwerySettings.NavigationStyle_Values getNavigationStyle() {
		return returnWith(AwerySettings.NAVIGATION_STYLE.getValue(), style ->
				(style == null || isTv()) ? AwerySettings.NavigationStyle_Values.MATERIAL : style);
	}

	public static Configuration getConfiguration(@NonNull Context context) {
		return context.getResources().getConfiguration();
	}

	public static Configuration getConfiguration() {
		return getConfiguration(getAnyContext());
	}

	public static void snackbar(
			@NonNull Activity activity,
			@StringRes int title,
			@StringRes int button,
			Runnable buttonCallback
	) {
		snackbar(activity, title, button, buttonCallback, Snackbar.LENGTH_SHORT);
	}

	public static void snackbar(
			@NonNull Activity activity,
			@StringRes int title,
			@StringRes int button,
			Runnable buttonCallback,
			int duration
	) {
		var context = getAnyContext();
		snackbar(activity, context.getString(title), context.getString(button), buttonCallback, duration);
	}

	public static void snackbar(@NonNull Activity activity, Object title, Object button, Runnable buttonCallback, int duration) {
		runOnUiThread(() -> {
			var titleText = title == null ? "null" : title.toString();
			var buttonText = button == null ? "null" : button.toString();

			var rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
			var snackbar = Snackbar.make(rootView, titleText, duration);

			if(buttonCallback != null) {
				snackbar.setAction(buttonText, view -> buttonCallback.run());
			}

			snackbar.getView().setOnClickListener(v -> snackbar.dismiss());
			snackbar.show();
		});
	}
}