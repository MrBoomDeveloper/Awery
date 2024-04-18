package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.mrboomdev.awery.extensions.ExtensionSettings;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistTagsQuery;
import com.mrboomdev.awery.extensions.support.js.JsManager;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.extensions.support.yomi.tachiyomi.TachiyomiManager;
import com.mrboomdev.awery.util.Callbacks;

import org.jetbrains.annotations.Contract;

import java.util.HashSet;
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
			Context context,
			@NonNull String behaviourId,
			Callbacks.Errorable<Set<SelectionItem>, Throwable> callback
	) {
		switch(behaviourId) {
			case "languages" -> {
				var locales = LocaleListCompat.getAdjustedDefault();
				var options = new HashSet<SelectionItem>();

				for(int i = 0; i < locales.size(); i++) {
					var locale = locales.get(i);
					if(locale == null) continue;

					options.add(new SelectionItem(locale.toLanguageTag(), locale.getDisplayLanguage(), i == 0));
				}

				callback.onResult(options, null);
			}

			case "excluded_tags" -> {
				var flags = AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT)
						? AnilistTagsQuery.ALL
						: AnilistTagsQuery.SAFE;

				AnilistTagsQuery.getTags(flags).executeQuery(context, tags -> {
					var excluded = AwerySettings.getInstance().getStringSet(AwerySettings.content.GLOBAL_EXCLUDED_TAGS);

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
			AppCompatActivity activity,
			@NonNull String behaviourId,
			Callbacks.Errorable<SettingsItem, Throwable> callback
	) {
		if(behaviourId.startsWith("extensions_")) {
			switch(behaviourId) {
				case "extensions_aweryjs" -> callback.onResult(
						new ExtensionSettings(activity, ExtensionsFactory.getManager(JsManager.class)), null);

				case "extensions_miru" -> callback.onResult(null,
						new IllegalArgumentException("Not now..."));

				case "extensions_cloudstream" -> callback.onResult(null,
						new IllegalArgumentException("Soon..."));

				case "extensions_aniyomi" -> callback.onResult(
						new ExtensionSettings(activity, ExtensionsFactory.getManager(AniyomiManager.class)), null);

				case "extensions_tachiyomi" -> callback.onResult(
						new ExtensionSettings(activity, ExtensionsFactory.getManager(TachiyomiManager.class)), null);

				default -> callback.onResult(null,
						new IllegalArgumentException("Unknown extensions screen: " + behaviourId));
			}
		} else {
			callback.onResult(null, new IllegalArgumentException("Unknown screen: " + behaviourId));
		}
	}

	public static void saveSelectionList(@NonNull String behaviourId, Set<SelectionItem> list) {
		switch(behaviourId) {
			case "languages" -> {
				var found = stream(list).filter(SelectionItem::isSelected).findFirst();
				if(found.isEmpty()) return;

				var locale = LocaleListCompat.forLanguageTags(found.get().getId());
				AppCompatDelegate.setApplicationLocales(locale);
			}

			case "excluded_tags" -> {
				var prefs = AwerySettings.getInstance();

				prefs.setStringSet(AwerySettings.content.GLOBAL_EXCLUDED_TAGS, stream(list)
						.filter(SelectionItem::isSelected)
						.map(SelectionItem::getTitle)
						.collect(Collectors.toSet()));

				prefs.saveAsync();
			}

			default -> Log.e(TAG, "Failed to save tags list");
		}
	}
}