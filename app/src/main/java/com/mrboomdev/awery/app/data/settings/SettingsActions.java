package com.mrboomdev.awery.app.data.settings;

import static com.mrboomdev.awery.app.App.showLoadingWindow;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.deleteFile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.app.data.Constants;
import com.mrboomdev.awery.app.services.BackupService;
import com.mrboomdev.awery.app.update.UpdatesManager;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.ui.activity.AboutActivity;
import com.mrboomdev.awery.ui.activity.ExperimentsActivity;
import com.mrboomdev.awery.ui.activity.setup.SetupActivity;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.CancelledException;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.Calendar;

import xcrash.XCrash;

@Deprecated(forRemoval = true)
public class SettingsActions {
	private static final String TAG = "SettingsActions";

	@Contract(pure = true)
	public static void run(@NonNull Setting item) {
		var actionName = item.getKey();
		if(actionName == null) return;

		switch(actionName) {
			case AwerySettings.CLEAR_IMAGE_CACHE -> {
				deleteFile(new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_IMAGE_CACHE));
				toast(R.string.cleared_successfully);
			}

			case AwerySettings.SETUP_THEME -> {
				var context = getAnyContext();
				var intent = new Intent(context, SetupActivity.class);
				intent.putExtra(SetupActivity.EXTRA_STEP, SetupActivity.STEP_THEMING);
				intent.putExtra(SetupActivity.EXTRA_FINISH_ON_COMPLETE, true);
				context.startActivity(intent);
			}

			case AwerySettings.ABOUT -> {
				var context = getAnyContext();
				var intent = new Intent(context, AboutActivity.class);
				context.startActivity(intent);
			}

			case AwerySettings.CLEAR_WEBVIEW_CACHE -> {
				deleteFile(new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_WEBVIEW_CACHE));
				toast(R.string.cleared_successfully);
			}

			case AwerySettings.CLEAR_NET_CACHE -> {
				deleteFile(new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_NET_CACHE));
				toast(R.string.cleared_successfully);
			}

			case AwerySettings.BACKUP -> {
				var date = Calendar.getInstance();

				var defaultName = "awery_backup_[" + date.get(Calendar.YEAR) + "_" +
						date.get(Calendar.MONTH) + "_" +
						date.get(Calendar.DATE) + "]_[" +
						date.get(Calendar.HOUR_OF_DAY) + "_" +
						date.get(Calendar.MINUTE) + "].awerybck";

				var context = getAnyContext();
				var intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType(MimeTypes.ANY.toString());
				intent.putExtra(Intent.EXTRA_TITLE, defaultName);

				startActivityForResult(context, intent, 0, ((resultCode, data) -> {
					if(resultCode != Activity.RESULT_OK) return;

					var backupIntent = new Intent(context, BackupService.class);
					backupIntent.setAction(BackupService.ACTION_BACKUP);
					backupIntent.setData(data.getData());
					context.startService(backupIntent);
				}));
			}

			case AwerySettings.RESTORE -> {
				var context = getAnyContext();
				var intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(MimeTypes.ANY.toString());
				var chooser = Intent.createChooser(intent, "Choose a backup file");

				startActivityForResult(context, chooser, 0, ((resultCode, data) -> {
					if(resultCode != Activity.RESULT_OK) return;

					var restoreIntent = new Intent(context, BackupService.class);
					restoreIntent.setAction(BackupService.ACTION_RESTORE);
					restoreIntent.setData(data.getData());
					context.startService(restoreIntent);
				}));
			}

			case AwerySettings.PLAYER_SYSTEM_SUBTITLES -> getAnyContext()
					.startActivity(new Intent(Settings.ACTION_CAPTIONING_SETTINGS));

			case AwerySettings.TRY_CRASH_NATIVE ->
					XCrash.testNativeCrash(false);

			case AwerySettings.TRY_CRASH_NATIVE_ASYNC ->
					thread(() -> XCrash.testNativeCrash(false));

			case AwerySettings.TRY_CRASH_JAVA ->
					XCrash.testJavaCrash(false);

			case AwerySettings.TRY_CRASH_JAVA_ASYNC ->
					thread(() -> XCrash.testJavaCrash(false));

			case AwerySettings.START_ONBOARDING -> {
				var context = getAnyContext();
				context.startActivity(new Intent(context, SetupActivity.class));
			}

			case AwerySettings.EXPERIMENTS -> {
				var context = getAnyContext();
				context.startActivity(new Intent(context, ExperimentsActivity.class));
			}

			case AwerySettings.CHECK_APP_UPDATE -> UpdatesManager.getAppUpdate().addCallback(new AsyncFuture.Callback<>() {
				private final Dialog window = showLoadingWindow();

				@Override
				public void onSuccess(UpdatesManager.Update update) {
					UpdatesManager.showUpdateDialog(update);
					window.dismiss();
				}

				@Override
				public void onFailure(Throwable t) {
					Log.e(TAG, "Failed to check for updates!", t);
					window.dismiss();

					if(t instanceof CancelledException) {
						toast(ExceptionDescriptor.getTitle(t, getAnyContext()), 1);
						return;
					}

					CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
							.setTitle("Failed to check for updates")
							.setPrefix(R.string.please_report_bug_app)
							.setThrowable(t)
							.build());
				}
			});

			default -> toast("Unknown action: " + actionName);
		}
	}
}