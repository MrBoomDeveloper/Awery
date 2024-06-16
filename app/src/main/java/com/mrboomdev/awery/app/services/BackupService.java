package com.mrboomdev.awery.app.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mrboomdev.awery.app.AweryNotifications;

import java.util.Objects;

public class BackupService extends Service {
	public static final String ACTION_BACKUP = "BACKUP";
	public static final String ACTION_RESTORE = "RESTORE";
	private final int notificationId = AweryNotifications.getNewNotificationId();

	@Override
	public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
		switch(Objects.requireNonNull(intent.getAction())) {
			case ACTION_BACKUP -> startBackup(intent.getData());
			case ACTION_RESTORE -> startRestore(intent.getData());
			default -> throw new IllegalArgumentException("Unknown action! " + intent.getAction());
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void startBackup(Uri uri) {
		startForeground(notificationId, AweryNotifications.Channel.BACKUP_PROGRESS.create(this)
				.setAutoCancel(false)
				.setOngoing(true)
				.setProgress(1, 0, true)
				.setContentTitle("Starting backup...")
				.build());
	}

	private void startRestore(Uri uri) {
		startForeground(notificationId, AweryNotifications.Channel.BACKUP_PROGRESS.create(this)
				.setAutoCancel(false)
				.setOngoing(true)
				.setProgress(1, 0, true)
				.setContentTitle("Starting restore...")
				.build());
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}