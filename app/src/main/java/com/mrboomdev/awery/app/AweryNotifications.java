package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationChannelGroupCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mrboomdev.awery.util.UniqueIdGenerator;

import org.jetbrains.annotations.Contract;

import java.lang.ref.WeakReference;

public class AweryNotifications {
	private static final UniqueIdGenerator idGenerator;
	private static WeakReference<NotificationManagerCompat> manager;

	static {
		// Android only accepts values above 0
		idGenerator = new UniqueIdGenerator();
		idGenerator.getInteger();
	}

	@NonNull
	private static NotificationManagerCompat getNotificationManager() {
		var result = manager != null ? manager.get() : null;

		if(result == null) {
			var context = getAnyContext();
			result = NotificationManagerCompat.from(context);
			manager = new WeakReference<>(result);
		}

		return result;
	}

	public static void registerNotificationChannels() {
		var manager = getNotificationManager();
		
		if(!manager.areNotificationsEnabled()) {
			return;
		}
		
		manager.createNotificationChannelGroupsCompat(stream(Group.values())
				.map(Group::toChannelGroup)
				.toList());
		
		manager.createNotificationChannelsCompat(stream(Channel.values())
				.map(Channel::toChannel)
				.toList());
	}

	public static int getNewNotificationId() {
		return idGenerator.getInteger();
	}

	public enum Group {
		BACKUPS("Backup & Restore"),
		DOWNLOADS("Downloads"),
		UPDATES("Updates");

		private final String name;

		Group(String name) {
			this.name = name;
		}

		@NonNull
		public NotificationChannelGroupCompat toChannelGroup() {
			return new NotificationChannelGroupCompat.Builder(name())
					.setName(name)
					.build();
		}
	}

	public enum Channel {
		BACKUP_PROGRESS("Backup progress", Group.BACKUPS,
				NotificationManagerCompat.IMPORTANCE_MIN),
		BACKUP_FAILED("Backup failed", Group.BACKUPS,
				NotificationManagerCompat.IMPORTANCE_DEFAULT),
		BACKUP_FINISHED("Backup finished", Group.BACKUPS,
				NotificationManagerCompat.IMPORTANCE_DEFAULT),

		RESTORE_PROGRESS("Restore progress", Group.BACKUPS,
				NotificationManagerCompat.IMPORTANCE_MIN),
		RESTORE_FAILED("Restore failed", Group.BACKUPS,
				NotificationManagerCompat.IMPORTANCE_DEFAULT),
		RESTORE_FINISHED("Restore finished", Group.BACKUPS,
				NotificationManagerCompat.IMPORTANCE_DEFAULT),

		DOWNLOAD_PROGRESS("Download progress", Group.DOWNLOADS,
				NotificationManagerCompat.IMPORTANCE_MIN),
		DOWNLOAD_PREPARE("Preparing for download", Group.DOWNLOADS,
				NotificationManagerCompat.IMPORTANCE_MIN),
		DOWNLOAD_FAILED("Download failed", Group.DOWNLOADS,
				NotificationManagerCompat.IMPORTANCE_DEFAULT),
		DOWNLOAD_FINISHED("Download finished", Group.DOWNLOADS,
				NotificationManagerCompat.IMPORTANCE_DEFAULT),

		APP_UPDATE("Awery update is available", Group.UPDATES,
				NotificationManagerCompat.IMPORTANCE_LOW),
		EXTENSION_UPDATE("Extension update is available", Group.UPDATES,
				NotificationManagerCompat.IMPORTANCE_LOW),

		PLAYER_CONTROLS("Player controls", null,
				NotificationManagerCompat.IMPORTANCE_MIN);

		private final Group group;
		private final String name;
		private final int importance;

		Channel(String name, Group group, int importance) {
			this.name = name;
			this.group = group;
			this.importance = importance;
		}

		@NonNull
		public NotificationChannelCompat toChannel() {
			return new NotificationChannelCompat.Builder(name(), importance)
					.setShowBadge(importance >= NotificationManagerCompat.IMPORTANCE_DEFAULT)
					.setGroup(group != null ? group.name() : null)
					.setName(name)
					.build();
		}

		@NonNull
		@Contract("_ -> new")
		public NotificationCompat.Builder create(Context context) {
			return new NotificationCompat.Builder(context, name());
		}
	}
}