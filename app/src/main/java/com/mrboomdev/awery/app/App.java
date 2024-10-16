package com.mrboomdev.awery.app;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.getAppContext;
import static com.mrboomdev.awery.app.AweryLifecycle.postRunnable;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.CATALOG_LIST_BLACKLIST;
import static com.mrboomdev.awery.data.Constants.CATALOG_LIST_HISTORY;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.os.strictmode.InstanceCountViolation;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.activity.SystemBarStyle;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.viewbinding.ViewBinding;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideCustomImageLoader;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.AweryDB;
import com.mrboomdev.awery.data.db.item.DBCatalogList;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.BrowserActivity;
import com.mrboomdev.awery.util.markdown.LinkifyPlugin;
import com.mrboomdev.awery.util.markdown.SpoilerPlugin;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import org.jetbrains.annotations.Contract;

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
import okhttp3.OkHttpClient;

@SuppressWarnings("StaticFieldLeak")
public class App extends Application {
	private static final WeakHashMap<Runnable, Object> backPressedCallbacks = new WeakHashMap<>();
	private static final String TAG = "App";
	private static Moshi moshi;
	private static AweryDB db;

	public static Moshi getMoshi() {
		if(moshi != null) {
			return moshi;
		}

		synchronized(App.class) {
			if(moshi != null) {
				return moshi;
			}

			return moshi = getMoshi();
		}
	}

	public static Moshi getMoshi(Object... adapters) {
		if(moshi != null) {
			return moshi;
		}

		synchronized(App.class) {
			if(moshi != null) {
				return moshi;
			}

			var builder = new Moshi.Builder()
					.addLast(new KotlinJsonAdapterFactory());

			for(var adapter : adapters) {
				builder.add(adapter);
			}

			return moshi = builder.build();
		}
	}

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

	/**
	 * Possible name param syntax:
	 * <P>{@code my_awesome_icon} - Will return an icon from the drawable directory</p>
	 * <p>{@code @mipmap/my_awesome_mipmap} - Will return an drawable from the mipmap directory</p>
	 * <p>{@code @color/my_color} - WIll return an {@link ColorDrawable} instance</p>
	 * @throws Resources.NotFoundException If no resource with such name was found
	 * @author MrBoomDev
	 */
	@Deprecated(forRemoval = true)
	@Contract(pure = true)
	public static Drawable resolveDrawable(Context context, @NonNull String name) throws Resources.NotFoundException {
		var clazz = name.startsWith("@mipmap/") ? R.mipmap.class
				: name.startsWith("@color/") ? R.color.class
				: R.drawable.class;

		var res = name;

		if(name.contains("/")) {
			res = name.substring(name.indexOf("/") + 1);
		}

		var id = getResourceId(clazz, res);

		if(clazz == R.color.class) {
			var color = ContextCompat.getColor(context, id);
			return new ColorDrawable(color);
		}

		return ContextCompat.getDrawable(context, id);
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

	/**
	 * Create a new ClipData holding data of the type
	 * {@link ClipDescription#MIMETYPE_TEXT_PLAIN}.
	 *
	 * @param label User-visible label for the clip data.
	 * @param content The actual text in the clip.
	 */
	public static void copyToClipboard(String label, String content) {
		copyToClipboard(ClipData.newPlainText(label, content));
	}

	/**
	 * Create a new ClipData holding an URI with MIME type
	 * {@link ClipDescription#MIMETYPE_TEXT_URILIST}.
	 *
	 * @param label User-visible label for the clip data.
	 * @param content The URI in the clip.
	 */
	public static void copyToClipboard(String label, Uri content) {
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

	public static void share(String text) {
		new ShareCompat.IntentBuilder(getAnyContext())
				.setType("text/plain")
				.setText(text)
				.startChooser();
	}

	/**
	 * A hacky method to fix the height, width of the dialog and color of the navigation bar.
	 * @author MrBoomDev
	 */
	@Deprecated(forRemoval = true)
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
			useLayoutParams(sheet, params -> params.width = dpPx(sheet, 400));
			window.setNavigationBarColor(SurfaceColors.SURFACE_1.getColor(context));
		} else {
			/* If we'll try to do this shit with the SideSheetDialog, it will get centered,
			   so we use different approaches for different dialog types.*/

			if(getConfiguration().screenWidthDp > 400) {
				window.setLayout(dpPx(context, 400), MATCH_PARENT);
			}
		}
	}

	@Deprecated(forRemoval = true)
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

	@Nullable
	public static String i18n(Class<?> clazz, String string) {
		var id = getResourceId(clazz, string);
		return id == 0 ? null : i18n(id);
	}

	public static String i18n(@StringRes int res) {
		return getAppContext().getString(res);
	}

	public static AweryDB getDatabase() {
		if(db != null) {
			return db;
		}

		synchronized(App.class) {
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
		runOnUiThread(() -> Toast.makeText(context, String.valueOf(text), duration).show());
	}

	public static void toast(Context context, Object text) {
		toast(context, text, 0);
	}

	public static void toast(Object text, int duration) {
		toast(getAppContext(), text, duration);
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
	 * There is a bug in an appcompat library which sometimes throws an {@link NullPointerException}.
	 * This method tries to do it without throwing any exceptions.
	 */
	@Deprecated(forRemoval = true)
	public static void setContentViewCompat(@NonNull Activity activity, @NonNull View view) {
		requireArgument(activity, "activity");
		requireArgument(view, "view");

		try {
			activity.setContentView(view);
		} catch(NullPointerException e) {
			Log.e(TAG, "Failed to setContentView!", e);

			// Caused by: java.lang.NullPointerException: Attempt to invoke virtual method
			//     'void androidx.appcompat.widget.ContentFrameLayout.setDecorPadding(int, int, int, int)' on a null object reference

			// at androidx.appcompat.app.AppCompatDelegateImpl.applyFixedSizeWindow(AppCompatDelegateImpl)

			postRunnable(() -> setContentViewCompat(activity, view));
		}
	}

	@Deprecated(forRemoval = true)
	public static void setContentViewCompat(@NonNull Activity activity, @NonNull ViewBinding view) {
		setContentViewCompat(activity, view.getRoot());
	}

	/**
	 * Safely enables the "Edge to edge" experience.
	 * I really don't know why, but sometimes it just randomly crashes!
	 * Because of it we have to rerun this method on a next frame.
	 * @author MrBoomDev
	 */
	@Deprecated(forRemoval = true)
	public static void enableEdgeToEdge(ComponentActivity context, SystemBarStyle statusBar, SystemBarStyle navBar) {
		try {
			if(statusBar == null) {
				statusBar = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT);
			}

			if(navBar == null) {
				navBar = SystemBarStyle.auto(
						EdgeToEdge.getDefaultLightScrim(),
						EdgeToEdge.getDefaultDarkScrim());
			}

			EdgeToEdge.enable(context, statusBar, navBar);
		} catch(RuntimeException e) {
			Log.e(TAG, "Failed to enable EdgeToEdge! Will retry a little bit later.", e);
			postRunnable(() -> enableEdgeToEdge(context));
		}
	}

	/**
	 * Safely enables the "Edge to edge" experience.
	 * I really don't know why, but sometimes it just randomly crashes!
	 * Because of it we have to rerun this method on a next frame.
	 * @author MrBoomDev
	 */
	@Deprecated(forRemoval = true)
	public static void enableEdgeToEdge(ComponentActivity context) {
		enableEdgeToEdge(context, null, null);
	}

	@Deprecated(forRemoval = true)
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

	@Deprecated(forRemoval = true)
	public static void addOnBackPressedListener(@NonNull Activity activity, Runnable callback) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			var onBackInvokedCallback = (OnBackInvokedCallback) callback::run;
			backPressedCallbacks.put(callback, onBackInvokedCallback);

			var dispatcher = activity.getOnBackInvokedDispatcher();
			dispatcher.registerOnBackInvokedCallback(0, onBackInvokedCallback);
		} else {
			if(activity instanceof OnBackPressedDispatcherOwner owner) {
				var onBackInvokedCallback = new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {
						callback.run();
					}
				};

				owner.getOnBackPressedDispatcher().addCallback(owner, onBackInvokedCallback);
				backPressedCallbacks.put(callback, onBackInvokedCallback);
			} else {
				throw new IllegalArgumentException("Activity must implement OnBackPressedDispatcherOwner!");
			}
		}
	}

	@Deprecated(forRemoval = true)
	public static int resolveAttrColor(@NonNull Context context, @AttrRes int res) {
		return MaterialColors.getColor(context, res, Color.BLACK);
	}

	@Deprecated(forRemoval = true)
	@SuppressLint("RestrictedApi")
	public static TypedValue resolveAttr(Context context, @AttrRes int res) {
		return MaterialAttributes.resolve(context, res);
	}

	public static boolean isLandscape(@NonNull Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static boolean isLandscape() {
		return isLandscape(getAnyContext());
	}

	public static boolean isTv() {
		var pm = getAnyContext().getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
	}

	public static void openUrl(@NonNull Context context, String url) {
		openUrl(context, url, false);
	}

	public static void openUrl(@NonNull Context context, String url, boolean forceInternal) {
		if(forceInternal) {
			var intent = new Intent(context, BrowserActivity.class);
			intent.putExtra(BrowserActivity.EXTRA_URL, url);
			context.startActivity(intent);
			return;
		}

		var customTabsIntent = new CustomTabsIntent.Builder()
				.setColorScheme(ThemeManager.isDarkModeEnabled()
						? CustomTabsIntent.COLOR_SCHEME_DARK
						: CustomTabsIntent.COLOR_SCHEME_LIGHT)
				.build();

		customTabsIntent.intent.setData(Uri.parse(url));

		var resolvedActivity = customTabsIntent.intent
				.resolveActivity(context.getPackageManager());

		if(resolvedActivity != null) {
			context.startActivity(customTabsIntent.intent, customTabsIntent.startAnimationBundle);
		} else {
			Log.e(TAG, "No external browser was found, launching a internal one.");
			var intent = new Intent(context, BrowserActivity.class);
			intent.putExtra(BrowserActivity.EXTRA_URL, url);
			context.startActivity(intent);
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

					CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
							.setPrefix(R.string.please_report_bug_app)
							.setThrowable(v)
							.build());
				}
			});

			threadPolicy.penaltyListener(Runnable::run, v -> {
				if(BuildConfig.DEBUG) {
					CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
							.setPrefix(R.string.please_report_bug_app)
							.setThrowable(v)
							.build());
				}
			});
		}

		StrictMode.setThreadPolicy(threadPolicy.build());
		StrictMode.setVmPolicy(vmPolicy.build());
	}

	@Override
	protected void attachBaseContext(@NonNull Context base) {
		super.attachBaseContext(base);
		CrashHandler.setupCrashListener(this);
	}

	@Override
	public void onCreate() {
		AweryNotifications.registerNotificationChannels();
		AweryLifecycle.init(this);
		ThemeManager.applyApp(this);
		setupStrictMode();

		super.onCreate();
		BigImageViewer.initialize(GlideCustomImageLoader.with(this));

		// Note: I'm so sorry. I've just waste the whole day to try fixing THIS SHIT!!!!
		// And in result in nothing! FUCKIN LIGHT THEME! WHY DOES IT EXIST!?!?!?!?!?!?!?!?!?!?
		// SYKA BLYYYYYYAAAAAAAT
		AwerySettings.USE_DARK_THEME.getValue(ThemeManager.isDarkModeEnabled());

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

		if(AwerySettings.LAST_OPENED_VERSION.getValue() < 1) {
			thread(() -> {
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
			});
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
		snackbar(activity, activity.getString(title), activity.getString(button), buttonCallback, duration);
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

	@NonNull
	public static Dialog showLoadingWindow() {
		var context = getAnyContext();

		var wrapper = new LinearLayoutCompat(context);
		wrapper.setGravity(Gravity.CENTER);

		var progress = new CircularProgressIndicator(context);
		progress.setIndeterminate(true);
		wrapper.addView(progress);

		var dialog = new AlertDialog.Builder(context)
				.setCancelable(false)
				.setView(wrapper)
				.show();

		requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
		return dialog;
	}
}