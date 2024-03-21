package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.AweryApp.stream;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistTagsQuery;
import com.mrboomdev.awery.util.CallbackUtil;

import org.jetbrains.annotations.Contract;

import java.util.Set;

import java9.util.stream.Collectors;

public class SettingsData {
	private static final String TAG = "SettingsData";

	public static class SelectionItem {
		private final String title, id;
		private boolean selected;

		public SelectionItem(String id, String title, boolean selected) {
			this.id = id;
			this.title = title;
			this.selected = selected;
		}

		public SelectionItem(String title, boolean selected) {
			this.title = title;
			this.selected = selected;
			this.id = null;
		}

		public String getTitle() {
			return title;
		}

		public String getId() {
			return id;
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

					callback.onResult(stream(tags).map(tag ->
							new SelectionItem(tag.getName(), excluded.contains(tag.getName())))
					.collect(Collectors.toSet()), null);
				}).catchExceptions(e -> callback.onResult(null, e));
			}

			default -> callback.onResult(null, new IllegalArgumentException("Failed to load tags list"));
		}
	}

	@Contract(pure = true)
	public static void getScreen(
			@NonNull String behaviourId,
			CallbackUtil.Errorable<SettingsItem, Throwable> callback
	) {
		if(behaviourId.startsWith("extensions_")) {
			switch(behaviourId) {
				case "extensions_aweryjs" -> callback.onResult(null, new IllegalArgumentException("Currently not supported"));
				case "extensions_aniyomi" -> callback.onResult(null, new IllegalArgumentException("Will be supported very soon!"));
				default -> callback.onResult(null, new IllegalArgumentException("Unknown extensions screen: " + behaviourId));
			}
		} else {
			callback.onResult(null, new IllegalArgumentException("Unknown screen: " + behaviourId));
		}
	}

	public static void saveSelectionList(@NonNull String behaviourId, Set<SelectionItem> list) {
		switch(behaviourId) {
			case "excluded_tags" -> {
				var prefs = AwerySettings.getInstance();

				prefs.setStringSet(AwerySettings.CONTENT_GLOBAL_EXCLUDED_TAGS, stream(list)
						.filter(SelectionItem::isSelected)
						.map(SelectionItem::getTitle)
						.collect(Collectors.toSet()));

				prefs.saveAsync();
			}

			default -> Log.e(TAG, "Failed to save tags list");
		}
	}
}