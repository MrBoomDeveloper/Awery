package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Objects;

import xcrash.Errno;
import xcrash.XCrash;

public class CrashHandler {
	private static final String TAG = "CrashHandler";

	public static void setup(Context context) {
		var xCrashParams = new XCrash.InitParameters()
				.enableNativeCrashHandler()
				.enableAnrCrashHandler()
				.enableJavaCrashHandler()
				.setJavaCallback((s, s1) -> handleError(CrashType.JAVA, s))
				.setNativeCallback((s, s1) -> handleError(CrashType.NATIVE, s))
				.setAnrCallback((s, s1) -> handleError(CrashType.ANR, s))
				.setAnrFastCallback((s, s1) -> handleError(CrashType.ANR, s))
				.setAppVersion(BuildConfig.VERSION_NAME);

		var result = switch(XCrash.init(context, xCrashParams)) {
			case Errno.INIT_LIBRARY_FAILED -> "Failed to initialize XCrash library!";
			case Errno.LOAD_LIBRARY_FAILED -> "Failed to load XCrash library!";
			case Errno.CONTEXT_IS_NULL -> "XCrash context is null!";
			default -> null;
		};

		if(result != null) {
			toast(context, result, Toast.LENGTH_LONG);
			Log.e(TAG, result);
		}
	}

	private enum CrashType {
		ANR, JAVA, NATIVE
	}

	private static void handleError(@NonNull CrashType type, String message) {
		var text = switch(type) {
			case ANR -> "Awery isn't responding ._.";
			case JAVA -> "Awery has crashed :(";
			case NATIVE -> "Something REALLY TERRIBLE has happened O_O";
		};

		toast(text, Toast.LENGTH_LONG);

		if(type != CrashType.ANR) {
			var crashFile = new File(getAnyContext().getExternalFilesDir(null), "crash.txt");

			try {
				if(!crashFile.exists()) crashFile.createNewFile();

				try(var writer = new FileWriter(crashFile)) {
					writer.write(message);
					Log.i(TAG, "Crash file saved successfully: " + crashFile.getAbsolutePath());
				}
			} catch(Throwable e) {
				Log.e(TAG, "Failed to write crash file!", e);
			}

			try {
				var activity = Objects.requireNonNull(getAnyActivity(Activity.class));
				showErrorDialog(activity, text, message, false, crashFile);
			} catch(Exception ignored) {}
		}
	}

	private static void handleUncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
		if(thread != Looper.getMainLooper().getThread()) {
			if(thread.getName().startsWith("Studio:")) {
				toast("Failed to send message to Android Studio!", Toast.LENGTH_LONG);
				return;
			}

			try {
				showErrorDialog(getAnyActivity(Activity.class), throwable, false, null);
			} catch(Exception ignored) {}

			toast("Unexpected error has happened!", Toast.LENGTH_LONG);
			return;
		}

		var crashFile = new File(getAnyContext().getExternalFilesDir(null), "crash.txt");
		var details = new ExceptionDescriptor(throwable);

		var activity = getAnyActivity(Activity.class);
		toast(activity, "App just crashed :(", Toast.LENGTH_LONG);

		try {
			if(!crashFile.exists()) crashFile.createNewFile();

			try(var writer = new FileWriter(crashFile)) {
				var moshi = new Moshi.Builder().add(ExceptionDescriptor.ADAPTER).build();
				var adapter = moshi.adapter(ExceptionDescriptor.class);

				writer.write(adapter.toJson(details));
				Log.i(TAG, "Crash file saved successfully: " + crashFile.getAbsolutePath());
			}
		} catch(Throwable e) {
			Log.e(TAG, "Failed to write crash file!", e);
		}

		if(activity != null) {
			activity.finishAffinity();
			return;
		}

		System.exit(0);
	}
	
	public static void showErrorDialog(Context context, Throwable throwable, boolean finishOnClose, File file) {
		var descriptor = new ExceptionDescriptor(throwable);
		var title = descriptor.getTitle(context);
		var description = descriptor.getMessage(context);
		showErrorDialog(context, title, description, finishOnClose, file);
	}

	public static void showErrorDialog(@NonNull Context context, String message, boolean finishOnClose, File file) {
		showErrorDialog(context, "An error has occurred!", message, finishOnClose, file);
	}

	public static void showErrorDialog(
			@NonNull Context context,
			String title,
			String message,
			boolean finishOnClose,
			File file
	) {
		runOnUiThread(() -> new MaterialAlertDialogBuilder(context)
				.setTitle(title.trim())
				.setMessage("Please send the following details to developers:\n\n" + message.trim())
				.setCancelable(false)
				.setOnDismissListener(dialog -> {
					if(file != null) file.delete();

					if(finishOnClose) {
						var activity = getAnyActivity(Activity.class);

						if(activity != null) {
							activity.finishAffinity();
							return;
						}

						System.exit(0);
					}
				})
				.setNeutralButton("Copy", (dialog, btn) -> {
					var clipboard = context.getSystemService(ClipboardManager.class);
					var clip = ClipData.newPlainText("crash report", message);
					clipboard.setPrimaryClip(clip);
				})
				.setNegativeButton("Share", (dialog, btn) -> {
					var intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT, "Awery crash report");
					intent.putExtra(Intent.EXTRA_TEXT, message);
					context.startActivity(Intent.createChooser(intent, "Share crash report..."));
				})
				.setPositiveButton("OK", (dialog, btn) -> dialog.dismiss())
				.show());
	}

	public static void reportIfExistsCrash(@NonNull Context context) {
		var crashFile = new File(context.getExternalFilesDir(null), "crash.txt");

		try {
			if(!crashFile.exists()) return;

			try(var reader = new BufferedReader(new FileReader(crashFile))) {
				StringBuilder result = new StringBuilder();
				String nextLine, message;

				while((nextLine = reader.readLine()) != null) {
					result.append(nextLine).append("\n");
				}

				try {
					var moshi = new Moshi.Builder().add(ExceptionDescriptor.ADAPTER).build();
					var adapter = moshi.adapter(ExceptionDescriptor.class);
					message = Objects.requireNonNull(adapter.fromJson(result.toString())).toString();
				} catch(Exception e) {
					try {
						var file = new File(result.toString().trim());

						try(var reader1 = new BufferedReader(new FileReader(file))) {
							StringBuilder result1 = new StringBuilder();
							String nextLine1;

							while((nextLine1 = reader1.readLine()) != null) {
								if(nextLine1.contains("ClassLoaderContext parent mismatch.")) continue;
								result1.append(nextLine1).append("\n");
							}

							message = result1.toString();
						}
					} catch(Exception ex) {
						message = result.toString();
					}
				}

				showErrorDialog(context, message, false, crashFile);
			}
		} catch(Throwable e) {
			Log.e(TAG, "Failed to read a crash file!", e);
		}
	}
}