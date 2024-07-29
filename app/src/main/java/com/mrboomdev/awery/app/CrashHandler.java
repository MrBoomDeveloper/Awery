package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryApp.copyToClipboard;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.getAppContext;
import static com.mrboomdev.awery.app.AweryLifecycle.restartApp;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textview.MaterialTextView;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import xcrash.Errno;
import xcrash.XCrash;

public class CrashHandler {
	private static final String TAG = "CrashHandler";

	protected static void setupCrashListener(Context context) {
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
				.setAnrCallback((s, s1) -> {Log.e(TAG, s + "\n\n" + s1); handleError(CrashType.ANR, s);})
				.setAppVersion(BuildConfig.VERSION_NAME);

		var result = switch(XCrash.init(context, xCrashParams)) {
			case Errno.INIT_LIBRARY_FAILED -> "Failed to initialize XCrash library!";
			case Errno.LOAD_LIBRARY_FAILED -> "Failed to load XCrash library!";
			case Errno.CONTEXT_IS_NULL -> "XCrash context is null!";
			default -> null;
		};

		if(result != null) {
			toast(result, Toast.LENGTH_LONG);
			Log.e(TAG, result);
		}
	}

	private enum CrashType {
		ANR, JAVA, NATIVE
	}

	private static void handleError(@NonNull CrashType type, String message) {
		toast(getAppContext().getString(switch(type) {
			case ANR -> {
				Log.e(TAG, "ANR error has happened. " + message);
				yield R.string.app_not_responding_restart;
			}

			case JAVA -> R.string.app_crash;
			case NATIVE -> R.string.something_terrible_happened;
		}), 1);

		if(type != CrashType.ANR) {
			var crashFile = new File(getAnyContext().getFilesDir(), "crash.txt");

			try {
				if(!crashFile.exists()) crashFile.createNewFile();

				try(var writer = new FileWriter(crashFile)) {
					writer.write(message);
					Log.i(TAG, "Crash file saved successfully: " + crashFile.getAbsolutePath());
				}
			} catch(Throwable e) {
				Log.e(TAG, "Failed to write crash file!", e);
			}
		}

		restartApp();
	}

	public static class CrashReport {
		private String title, message, prefix, pohuiStringThrowable;
		private Throwable throwable;
		private File file;
		private Runnable dismissCallback;

		public static class Builder {
			private final CrashReport report = new CrashReport();

			public Builder setMessage(String message) {
				report.message = message;
				return this;
			}

			public Builder setPohuiStringThrowable(String t) {
				report.pohuiStringThrowable = t;
				return this;
			}

			public Builder setMessage(@StringRes int prefix) {
				report.message = getAnyContext().getString(prefix);
				return this;
			}

			public Builder setPrefix(@StringRes int prefix) {
				report.prefix = getAnyContext().getString(prefix);
				return this;
			}

			public Builder setThrowable(Throwable throwable) {
				report.throwable = throwable;
				return this;
			}

			public Builder setDismissCallback(Runnable dismissCallback) {
				report.dismissCallback = dismissCallback;
				return this;
			}

			public Builder setFile(File file) {
				report.file = file;
				return this;
			}

			public Builder setTitle(String title) {
				report.title = title;
				return this;
			}

			public Builder setTitle(@StringRes int title) {
				report.title = getAnyContext().getString(title);
				return this;
			}

			public CrashReport build() {
				return report;
			}
		}
	}

	public static void showErrorDialog(CrashReport report) {
		showErrorDialog(requireNonNull(getAnyActivity(AppCompatActivity.class)), report);
	}

	@NonNull
	private static View createExpandable(Context context, String message) {
		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);

		var expander = new MaterialTextView(context);
		expander.setBackgroundResource(R.drawable.ripple_round_you);
		expander.setText("Click to see the exception");
		expander.setClickable(true);
		expander.setFocusable(true);
		linear.addView(expander);

		setPadding(expander, dpPx(expander, 16));
		setMargin(expander, dpPx(expander, -16));
		setBottomMargin(expander, dpPx(expander, 0));

		var content = new MaterialTextView(context);
		content.setTextIsSelectable(true);
		content.setText(message);
		content.setVisibility(View.GONE);
		linear.addView(content);

		expander.setOnClickListener(v -> {
			var makeVisible = content.getVisibility() != View.VISIBLE;
			expander.setText(makeVisible ? "Click to hide the exception" : "Click to see the exception");
			content.setVisibility(makeVisible ? View.VISIBLE : View.GONE);
		});

		return linear;
	}

	public static void showErrorDialog(@NonNull Context context, CrashReport report) {
		runOnUiThread(() -> {
			View contentView = null;

			if(report.throwable != null && report.pohuiStringThrowable != null) {
				throw new IllegalStateException("You can't use both things!");
			}

			if(report.throwable != null) {
				var unwrapped = ExceptionDescriptor.unwrap(report.throwable);
				var title = ExceptionDescriptor.getTitle(unwrapped, context);
				var message = ExceptionDescriptor.getMessage(unwrapped, context);

				if(!ExceptionDescriptor.isUnknownException(unwrapped)) {
					if(report.title == null) {
						report.title = title;
					}

					if(report.message == null) {
						report.message = message;
					}
				}

				contentView = createExpandable(context, message);
			}

			if(report.pohuiStringThrowable != null) {
				contentView = createExpandable(context, report.pohuiStringThrowable);
			}

			if(report.prefix != null) {
				report.message = report.message == null ? report.prefix
						: report.message + "\n\n" + report.prefix;
			}

			var builder = new DialogBuilder(context)
					.setCancelable(false)
					.setOnDismissListener(dialog -> {
						if(report.file != null) {
							report.file.delete();
						}

						if(report.dismissCallback != null) {
							report.dismissCallback.run();
						}
					})
					.setNeutralButton(R.string.copy, dialog ->
							copyToClipboard(context.getString(R.string.crash_report), report.message))
					.setNegativeButton(R.string.share, dialog -> {
						var newFile = new File(context.getFilesDir(), "crash_report.txt");
						var intent = new Intent(Intent.ACTION_SEND);
						intent.setType("*/*");

						try {
							newFile.delete();
							newFile.createNewFile();

							try(var writer = new FileWriter(newFile)) {
								writer.write(report.message);
							}

							intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
									context, BuildConfig.FILE_PROVIDER, newFile));
						} catch(IOException e) {
							Log.e(TAG, "Failed to write a file!", e);
							intent.putExtra(Intent.EXTRA_TEXT, report.message);
						}

						context.startActivity(Intent.createChooser(intent, "Share crash report"));
					})
					.setPositiveButton("OK", DialogBuilder::dismiss);

			if(contentView != null) {
				builder.addView(contentView);
			}

			if(report.title != null) {
				builder.setTitle(report.title.trim());
			}

			if(report.message != null) {
				builder.setMessage(report.message.trim());
			}

			builder.show();
		});
	}

	public static void reportIfCrashHappened(@NonNull Context context, Runnable dismissCallback) {
		var crashFile = new File(context.getFilesDir(), "crash.txt");

		try {
			if(!crashFile.exists()) {
				if(dismissCallback != null) {
					dismissCallback.run();
				}

				return;
			}

			try(var reader = new BufferedReader(new FileReader(crashFile))) {
				StringBuilder result = new StringBuilder();
				ExceptionDescriptor descriptor = null;
				String nextLine, message = null;

				while((nextLine = reader.readLine()) != null) {
					result.append(nextLine).append("\n");
				}

				try {
					descriptor = Parser.fromString(ExceptionDescriptor.class, result.toString());
				} catch(IOException e) {
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

				var report = new CrashReport.Builder()
						.setTitle(R.string.app_crash)
						.setDismissCallback(dismissCallback)
						.setFile(crashFile);

				if(descriptor != null) {
					report.setPrefix(R.string.please_report_bug_app);
					report.setThrowable(descriptor.getThrowable());
				}

				if(message != null) {
					report.setPrefix(R.string.please_report_bug_app);
					report.setPohuiStringThrowable(message);
				}

				showErrorDialog(report.build());
			}
		} catch(Throwable e) {
			Log.e(TAG, "Failed to read a crash file!", e);
			if(dismissCallback != null) dismissCallback.run();
		}
	}
}