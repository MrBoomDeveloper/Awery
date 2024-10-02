package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.App.getDatabase;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.CATALOG_LIST_BLACKLIST;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.data.db.item.DBCatalogMedia;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.ui.dialogs.MediaBookmarkDialog;

import org.jetbrains.annotations.Contract;

import java.util.Collection;

public class MediaUtils {

	@Deprecated(forRemoval = true)
	public static void launchMediaActivity(Context context, @NonNull CatalogMedia media, @MediaActivity.Action String action) {
		var intent = new Intent(context, MediaActivity.class);
		intent.putExtra(MediaActivity.EXTRA_MEDIA, media);
		intent.putExtra(MediaActivity.EXTRA_ACTION, action);
		context.startActivity(intent);
	}

	@Deprecated(forRemoval = true)
	public static void launchMediaActivity(Context context, CatalogMedia media) {
		launchMediaActivity(context, media, MediaActivity.EXTRA_ACTION_INFO);
	}

	@NonNull
	public static Collection<? extends CatalogMedia> filterMediaSync(
			@NonNull Collection<? extends CatalogMedia> items
	) {
		return stream(items)
				.filter(item -> !isMediaFilteredSync(item))
				.toList();
	}

	public static void filterMedia(
			@NonNull Collection<? extends CatalogMedia> items,
			Callbacks.Callback1<Collection<? extends CatalogMedia>> callback
	) {
		thread(() -> callback.run(filterMediaSync(items)));
	}

	public static boolean isMediaFilteredSync(@NonNull CatalogMedia media) {
		var prefs = NicePreferences.getPrefs();
		var badTags = prefs.getStringSet(AwerySettings.GLOBAL_EXCLUDED_TAGS);
		var saved = App.getDatabase().getMediaProgressDao().get(media.globalId);

		if(saved != null) {
			if(AwerySettings.HIDE_LIBRARY_ENTRIES.getValue() && saved.getListsCount() > 0) {
				return true;
			}

			if(saved.isInList(CATALOG_LIST_BLACKLIST)) {
				return true;
			}
		}

		return media.tags != null && stream(media.tags)
				.map(CatalogTag::getName)
				.anyMatch(badTags::contains);
	}

	@Contract(pure = true)
	public static void isMediaFiltered(@NonNull CatalogMedia media, Callbacks.Callback1<Boolean> callback) {
		thread(() -> callback.run(isMediaFilteredSync(media)));
	}

	public static void blacklistMedia(CatalogMedia media, Runnable callback) {
		thread(() -> {
			var listsDao = getDatabase().getMediaProgressDao();
			var mediaDao = getDatabase().getMediaDao();

			var lists = listsDao.get(media.globalId);
			if(lists == null) lists = new CatalogMediaProgress(media.globalId);
			lists.addToList(CATALOG_LIST_BLACKLIST);

			mediaDao.insert(DBCatalogMedia.fromCatalogMedia(media));
			listsDao.insert(lists);

			runOnUiThread(callback);
		});
	}

	@Deprecated(forRemoval = true)
	public static void openMediaBookmarkMenu(Context context, CatalogMedia media) {
		new MediaBookmarkDialog(media).show(context);
	}
}