package com.mrboomdev.awery.app.services;

import static com.mrboomdev.awery.app.AweryApp.showLoadingWindow;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.restartApp;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class BackupService extends Service {
	public static final String ACTION_BACKUP = "BACKUP";
	public static final String ACTION_RESTORE = "RESTORE";
	private static final String TAG = "BackupService";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null || intent.getAction() == null) {
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}

		switch(intent.getAction()) {
			case ACTION_BACKUP -> startBackup(intent.getData());
			case ACTION_RESTORE -> startRestore(intent.getData());
			default -> throw new IllegalArgumentException("Unknown action! " + intent.getAction());
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void startBackup(Uri into) {
		var popup = showLoadingWindow();

		new Thread(() -> {
			try {
				var map = new HashMap<File, String>();
				var backupDirs = new String[] { "shared_prefs", "databases" };

				for(var dir : backupDirs) {
					var list = new File(getFilesDir(), "../" + dir).listFiles();
					if(list == null) continue;

					for(var file : list) {
						map.put(file, dir + "/" + file.getName());
					}
				}

				FileUtil.zip(map, into);
				toast("Created backup successfully!");
				runOnUiThread(popup::dismiss);
				stopSelf();

			} catch(IOException e) {
				Log.e(TAG, "Failed to create an backup", e);

				runOnUiThread(() -> {
					popup.dismiss();

					CrashHandler.showErrorDialog(getAnyActivity(AppCompatActivity.class),
							"Failed to create an backup", e);
				});

				stopSelf();
			}
		}).start();
	}

	private void startRestore(Uri uri) {
		var window = showLoadingWindow();

		new Thread(() -> {
			try {
				FileUtil.unzip(uri, new File(getFilesDir(), ".."));
				toast("Restored backup successfully!");
				restartApp();
				stopSelf();
			} catch(IOException e) {
				Log.e(TAG, "Failed to restore an backup", e);

				runOnUiThread(() -> {
					window.dismiss();

					CrashHandler.showErrorDialog(getAnyActivity(AppCompatActivity.class),
							"Failed to restore an backup", e);
				});

				stopSelf();
			}
		}).start();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}