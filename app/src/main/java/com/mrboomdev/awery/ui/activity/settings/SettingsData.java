package com.mrboomdev.awery.ui.activity.settings;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.anilist.query.AnilistTagsQuery;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.util.CallbackUtil;

import java.util.Set;
import java.util.stream.Collectors;

public class SettingsData {
	private static final String TAG = "SettingsData";

	public static class SelectionItem {
		private final String title;
		private boolean selected;

		public SelectionItem(String title, boolean selected) {
			this.title = title;
			this.selected = selected;
		}

		public String getTitle() {
			return title;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}
	}

	public static void getSelectionList(
			@NonNull String behaviourId,
			CallbackUtil.Errorable<Set<SelectionItem>, Throwable> callback
	) {
		switch(behaviourId) {
			case "excluded_tags" -> {
				var flags = AwerySettings.getInstance().getBoolean(AwerySettings.ADULT_CONTENT)
						? AnilistTagsQuery.ALL
						: AnilistTagsQuery.SAFE;

				AnilistTagsQuery.getTags(flags).executeQuery(tags -> {
					var excluded = AwerySettings.getInstance().getStringSet(AwerySettings.CONTENT_GLOBAL_EXCLUDED_TAGS);

					callback.onResult(tags.stream().map(tag ->
							new SelectionItem(tag.getName(), excluded.contains(tag.getName())))
					.collect(Collectors.toSet()), null);
				}).catchExceptions(e -> callback.onResult(null, e));
			}

			default -> callback.onResult(null, new IllegalArgumentException("Failed to load tags list"));
		}
	}

	public static void saveSelectionList(@NonNull String behaviourId, Set<SelectionItem> list) {
		switch(behaviourId) {
			case "excluded_tags" -> {
				var prefs = AwerySettings.getInstance();

				prefs.setStringSet(AwerySettings.CONTENT_GLOBAL_EXCLUDED_TAGS, list.stream()
						.filter(SelectionItem::isSelected)
						.map(SelectionItem::getTitle)
						.collect(Collectors.toSet()));

				prefs.saveAsync();
			}

			default -> Log.e(TAG, "Failed to save tags list");
		}
	}
}