package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";

	@Override
	public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
		if(thread != Looper.getMainLooper().getThread()) {
			Log.e(TAG, "THREAD WAS KILLED! [ Thread name: "
					+ thread.getName() + ", Thread id: "
					+ thread.getId() + " ]", throwable);

			var activity = getAnyActivity();

			if(activity != null) {
				if(thread.getName().startsWith("Studio:")) {
					toast(activity, "Failed to send message to Android Studio!", Toast.LENGTH_LONG);
					return;
				}

				toast(activity, "Unexpected error has happened!", Toast.LENGTH_LONG);
			}

			return;
		}

		var crashFile = new File(getAnyContext().getExternalFilesDir(null), "crash.txt");
		var details = new ExceptionDescriptor(throwable);
		var activity = getAnyActivity();

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
			toast(activity, "App just crashed :(", Toast.LENGTH_LONG);
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
		new MaterialAlertDialogBuilder(context)
				.setTitle(title)
				.setMessage("Please send the following details to developers:\n\n" + message)
				.setCancelable(false)
				.setOnDismissListener(dialog -> {
					if(file != null) file.delete();

					if(finishOnClose) {
						var activity = getAnyActivity();

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
				.show();
	}

	public static void reportIfExistsCrash(@NonNull Context context) {
		var crashFile = new File(context.getExternalFilesDir(null), "crash.txt");

		try {
			if(!crashFile.exists()) return;

			try(var reader = new BufferedReader(new FileReader(crashFile))) {
				StringBuilder result = new StringBuilder();
				String nextLine;

				while((nextLine = reader.readLine()) != null) {
					result.append(nextLine);
				}

				var moshi = new Moshi.Builder().add(ExceptionDescriptor.ADAPTER).build();
				var adapter = moshi.adapter(ExceptionDescriptor.class);
				var details = adapter.fromJson(result.toString());
				if(details == null) return;

				showErrorDialog(context, details.toString(), false, crashFile);
			}
		} catch(Throwable e) {
			Log.e(TAG, "Failed to read a crash file!", e);
		}
	}
}