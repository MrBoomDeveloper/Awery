package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationChannelGroupCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;

public class AweryNotifications {
	private static final UniqueIdGenerator idGenerator = new UniqueIdGenerator();

	static {
		// Android only accepts values above 0
		idGenerator.getInteger();
	}

	public static boolean registerNotificationChannels(Context context) {
		var manager = NotificationManagerCompat.from(context);
		
		if(!manager.areNotificationsEnabled()) {
			return false;
		}
		
		manager.createNotificationChannelGroupsCompat(stream(Group.values())
				.map(Group::toChannelGroup)
				.toList());
		
		manager.createNotificationChannelsCompat(stream(Channel.values())
				.map(Channel::toChannel)
				.toList());
		
		return true;
	}

	public static int getNewNotificationId() {
		return idGenerator.getInteger();
	}

	public enum Group {
		DOWNLOADS("downloads", "Downloads"),
		UPDATES("updates", "Updates");

		private final String groupId, name;

		Group(String groupId, String name) {
			this.groupId = groupId;
			this.name = name;
		}

		@NonNull
		public NotificationChannelGroupCompat toChannelGroup() {
			return new NotificationChannelGroupCompat.Builder(groupId)
					.setName(name)
					.build();
		}
	}

	public enum Channel {
		DOWNLOAD_PROGRESS("download_progress", "Downloading progress", Group.DOWNLOADS, NotificationManagerCompat.IMPORTANCE_MIN),
		DOWNLOAD_PREPARE("download_prepare", "Preparing for download", Group.DOWNLOADS, NotificationManagerCompat.IMPORTANCE_MIN),
		DOWNLOAD_FAILED("download_failed", "Download failed", Group.DOWNLOADS, NotificationManagerCompat.IMPORTANCE_DEFAULT),
		DOWNLOAD_FINISHED("download_finished", "Download finished", Group.DOWNLOADS, NotificationManagerCompat.IMPORTANCE_DEFAULT),
		APP_UPDATE("app_update", "Awery update is available", Group.UPDATES, NotificationManagerCompat.IMPORTANCE_LOW),
		EXTENSION_UPDATE("extension_update", "Extension update is available", Group.UPDATES, NotificationManagerCompat.IMPORTANCE_LOW),
		PLAYER_CONTROLS("player_controls", "Player controls", null, NotificationManagerCompat.IMPORTANCE_MIN);

		private final Group group;
		private final String channelId, name;
		private final int importance;

		Channel(String channelId, String name, Group group, int importance) {
			this.channelId = channelId;
			this.name = name;
			this.group = group;
			this.importance = importance;
		}

		public String getChannelId() {
			return channelId;
		}

		@NonNull
		public NotificationChannelCompat toChannel() {
			return new NotificationChannelCompat.Builder(channelId, importance)
					.setShowBadge(importance >= NotificationManagerCompat.IMPORTANCE_DEFAULT)
					.setGroup(group != null ? group.groupId : null)
					.setName(name)
					.build();
		}
	}
}