package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.restartApp;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Objects;

import xcrash.Errno;
import xcrash.XCrash;

public class CrashHandler {
	private static final String TAG = "CrashHandler";

	protected static void setup(Context context) {
		var xCrashParams = new XCrash.InitParameters()
				.enableNativeCrashHandler()
				.enableAnrCrashHandler()
				.enableJavaCrashHandler()
				.setJavaDumpNetworkInfo(false)
				.setJavaDumpAllThreads(false)
				.setNativeDumpNetwork(false)
				.setNativeDumpAllThreads(false)
				.setJavaDumpFds(false)
				.setNativeDumpElfHash(false)
				.setAnrDumpFds(false)
				.setNativeDumpFds(false)
				.setNativeDumpMap(false)
				.setJavaCallback((s, s1) -> handleError(CrashType.JAVA, s))
				.setNativeCallback((s, s1) -> handleError(CrashType.NATIVE, s))
				.setAnrCallback((s, s1) -> handleError(CrashType.ANR, s))
				.setAnrFastCallback((s, s1) -> handleError(CrashType.ANR_FAST, s))
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
		ANR, JAVA, NATIVE, ANR_FAST
	}

	private static void handleError(@NonNull CrashType type, String message) {
		var text = switch(type) {
			case ANR -> "Awery isn't responding for a long time ._.";

			case ANR_FAST -> {
				toast("Awery isn't responding. Trying to restart ._.", 1);
				restartApp();
				yield null;
			}

			case JAVA -> "Awery has crashed :(";
			case NATIVE -> "Something REALLY TERRIBLE has happened O_O";
		};

		toast(text, 1);

		if(type != CrashType.ANR && type != CrashType.ANR_FAST) {
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
				showErrorDialogImpl(activity, text, message, crashFile);
			} catch(Exception ignored) {}
		}
	}
	
	public static void showErrorDialog(Context context, Throwable throwable) {
		var descriptor = new ExceptionDescriptor(throwable);

		showErrorDialogImpl(context,
				descriptor.getTitle(context),
				context.getString(R.string.please_report_bug_app)
						+ "\n\n" + descriptor.getMessage(context), null);
	}

	public static void showErrorDialog(Context context, String title, String messagePrefix, Throwable throwable) {
		if(throwable != null) {
			var descriptor = new ExceptionDescriptor(throwable);
			var description = descriptor.getMessage(context);
			showErrorDialogImpl(context, title, messagePrefix + "\n\n" + description, null);
		} else {
			showErrorDialogImpl(context, title, messagePrefix, null);
		}
	}

	public static void showErrorDialog(Context context, String title, Throwable throwable) {
		showErrorDialog(context, title, context.getString(R.string.please_report_bug_app), throwable);
	}

	private static void showErrorDialogImpl(
			@NonNull Context context,
			String title,
			String message,
			File file
	) {
		runOnUiThread(() -> new MaterialAlertDialogBuilder(context)
				.setTitle(title.trim())
				.setMessage(message.trim())
				.setCancelable(false)
				.setOnDismissListener(dialog -> {
					if(file != null) file.delete();
				})
				.setNeutralButton("Copy", (dialog, btn) -> {
					var clipboard = context.getSystemService(ClipboardManager.class);
					var clip = ClipData.newPlainText("Crash report", message);
					clipboard.setPrimaryClip(clip);
				})
				.setNegativeButton(R.string.share, (dialog, btn) -> {
					var intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT, "Crash report");
					intent.putExtra(Intent.EXTRA_TEXT, message);
					context.startActivity(Intent.createChooser(intent, "Share crash report..."));
				})
				.setPositiveButton("OK", (dialog, btn) -> dialog.dismiss())
				.show());
	}

	public static void reportIfCrashHappened(@NonNull Context context) {
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
					message = Parser.fromString(ExceptionDescriptor.class, result.toString()).toString();
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

				var content = context.getString(R.string.please_report_bug_app) + "\n\n" + message.trim();
				showErrorDialogImpl(context, "Awery has crashed!", content, crashFile);
			}
		} catch(Throwable e) {
			Log.e(TAG, "Failed to read a crash file!", e);
		}
	}
}