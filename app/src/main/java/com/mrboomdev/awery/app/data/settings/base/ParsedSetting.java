package com.mrboomdev.awery.app.data.settings.base;

import static com.mrboomdev.awery.app.App.showLoadingWindow;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.deleteFile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.app.data.Constants;
import com.mrboomdev.awery.app.services.BackupService;
import com.mrboomdev.awery.app.update.UpdatesManager;
import com.mrboomdev.awery.ext.data.Image;
import com.mrboomdev.awery.ext.data.Selection;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.ui.activity.AboutActivity;
import com.mrboomdev.awery.ui.activity.ExperimentsActivity;
import com.mrboomdev.awery.ui.activity.setup.SetupActivity;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.CancelledException;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.squareup.moshi.Json;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Calendar;

import xcrash.XCrash;

/**
 * Used for internal settings which are being mapped from settings.json
 */
@SuppressWarnings("unused")
public class ParsedSetting extends Setting implements AndroidSetting {
	private static final String TAG = "ParsedSetting";
	private String icon, action;
	private boolean restart;
	@Json(name = "show_if")
	private String showIf;
	@Json(ignore = true)
	private Object value;
	@Json(name = "integer_value")
	private Integer integerValue;
	@Json(name = "string_value")
	private String stringValue;
	@Json(name = "boolean_value")
	private Boolean booleanValue;

	@Override
	public @Nullable Object getValue() {
		return value;
	}

	public void restoreSavedValues() {
		if(getKey() != null && getType() != null) {
			switch(getType()) {
				case STRING, SELECT -> setValue(getPrefs().getString(getKey(), stringValue));
				case INTEGER, SELECT_INTEGER, COLOR -> setValue(getPrefs().getInteger(getKey(), integerValue));
				case BOOLEAN, SCREEN_BOOLEAN -> setValue(getPrefs().getBoolean(getKey(), booleanValue));

				case EXCLUDABLE -> {
					if(stringValue != null) {
						setValue(Selection.State.valueOf(stringValue));
					}
				}
			}
		}
	}

	private void saveValue() {
		if(getKey() != null && getType() != null) {
			switch(getType()) {
				case STRING, SELECT -> getPrefs().setValue(getKey(), (String) value);
				case INTEGER, SELECT_INTEGER, COLOR -> getPrefs().setValue(getKey(), (Integer) value);
				case BOOLEAN, SCREEN_BOOLEAN -> getPrefs().setValue(getKey(), (Boolean) value);
			}
		}
	}

	@Override
	public void setValue(Object o) {
		if(getType() != null) {
			getType().checkIfValidValue(o);
		}

		saveValue();
		this.value = o;
	}

	@Override
	public void onClick() {
		if(action == null) return;

		switch(action) {
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

			default -> toast("Unknown action: " + action);
		}
	}

	@Override
	public @Nullable Image getIcon() {
		if(icon != null) {
			return new AndroidImage(icon);
		}

		return null;
	}

	@Override
	public boolean isRestartRequired() {
		return restart;
	}

	@Override
	public boolean isVisible() {
		if(showIf != null) {
			var requirements = showIf.split(",");

			for(var requirement : requirements) {
				if(!App.isRequirementMet(requirement)) return false;
			}
		}

		return true;
	}
}