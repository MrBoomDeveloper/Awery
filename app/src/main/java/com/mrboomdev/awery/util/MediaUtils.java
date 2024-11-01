package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.data.Constants.CATALOG_LIST_BLACKLIST;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.app.data.db.item.DBCatalogMedia;
import com.mrboomdev.awery.app.data.settings.NicePreferences;
import com.mrboomdev.awery.ext.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.ext.data.CatalogTag;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.safeargsnext.SafeArgsIntent;

import org.jetbrains.annotations.Contract;

import java.util.Collection;

public class MediaUtils {

	@Deprecated(forRemoval = true)
	public static void launchMediaActivity(@NonNull Context context, @NonNull CatalogMedia media, String action) {
		context.startActivity(new SafeArgsIntent<>(context, MediaActivity.class, new MediaActivity.Extras(
				media, action != null ? MediaActivity.Action.valueOf(action) : null
		)));
	}

	@Deprecated(forRemoval = true)
	public static void launchMediaActivity(@NonNull Context context, CatalogMedia media) {
		launchMediaActivity(context, media, null);
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
			Callback1<Collection<? extends CatalogMedia>> callback
	) {
		thread(() -> callback.run(filterMediaSync(items)));
	}

	public static boolean isMediaFilteredSync(@NonNull CatalogMedia media) {
		var prefs = NicePreferences.getPrefs();
		var badTags = prefs.getStringSet(AwerySettings.GLOBAL_EXCLUDED_TAGS);
		var saved = App.Companion.getDatabase().getMediaProgressDao().get(media.getGlobalId());

		if(saved != null) {
			if(AwerySettings.HIDE_LIBRARY_ENTRIES.getValue() && saved.getListsCount() > 0) {
				return true;
			}

			if(saved.isInList(CATALOG_LIST_BLACKLIST)) {
				return true;
			}
		}

		return media.getTags() != null && stream(media.getTags())
				.map(CatalogTag::getName)
				.anyMatch(badTags::contains);
	}
	
	public interface Callback1<T> {
		void run(T arg);
	}

	@Contract(pure = true)
	public static void isMediaFiltered(@NonNull CatalogMedia media, Callback1<Boolean> callback) {
		thread(() -> callback.run(isMediaFilteredSync(media)));
	}

	public static void blacklistMedia(CatalogMedia media, Runnable callback) {
		thread(() -> {
			var listsDao = App.Companion.getDatabase().getMediaProgressDao();
			var mediaDao = App.Companion.getDatabase().getMediaDao();

			var lists = listsDao.get(media.getGlobalId());
			if(lists == null) lists = new CatalogMediaProgress(media.getGlobalId());
			lists.addToList(CATALOG_LIST_BLACKLIST);

			mediaDao.insert(DBCatalogMedia.fromCatalogMedia(media));
			listsDao.insert(lists);

			runOnUiThread(callback);
		});
	}
}