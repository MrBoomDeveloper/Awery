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
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.util.Selection;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import java9.util.stream.Collectors;

public class SettingsData {
	private static final String TAG = "SettingsData";

	public static void getSelectionList(
			Context context,
			@NonNull String listId,
			Callbacks.Errorable<Selection<Selection.Selectable<String>>, Throwable> callback
	) {
		switch(listId) {
			case "languages" -> callback.onResult(new Selection<>(new ArrayList<>() {{
				var locales = LocaleListCompat.getAdjustedDefault();

				for(int i = 0; i < locales.size(); i++) {
					var locale = locales.get(i);
					if(locale == null) continue;

					add(new Selection.Selectable<>(
							locale.toLanguageTag(),
							locale.getDisplayLanguage(),
							i == 0 ? Selection.State.SELECTED : Selection.State.UNSELECTED));
				}
			}}), null);

			case "excluded_tags" -> {
				var flags = AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT)
						? AnilistTagsQuery.ALL
						: AnilistTagsQuery.SAFE;

				AnilistTagsQuery.getTags(flags).executeQuery(context, tags -> {
					var excluded = AwerySettings.getInstance().getStringSet(AwerySettings.content.GLOBAL_EXCLUDED_TAGS);

					callback.onResult(stream(tags).map(tag -> {
						var state = excluded.contains(tag.getName()) ?
								Selection.State.SELECTED :
								Selection.State.UNSELECTED;

						return new Selection.Selectable<>(tag.getName(), tag.getName(), state);
					}).collect(Selection.collect()), null);
				}).catchExceptions(e -> callback.onResult(null, e));
			}

			default -> callback.onResult(null, new IllegalArgumentException("Failed to load tags list"));
		}
	}

	public static void saveSelectionList(@NonNull String listId, Selection<Selection.Selectable<String>> list) {
		switch(listId) {
			case "languages" -> {
				var found = list.get(Selection.State.SELECTED);
				if(found == null) return;

				var locale = LocaleListCompat.forLanguageTags(found.getId());
				AppCompatDelegate.setApplicationLocales(locale);
			}

			case "excluded_tags" -> {
				var items = stream(list.getAll(Selection.State.SELECTED))
						.map(Selection.Selectable::getItem)
						.collect(Collectors.toSet());

				AwerySettings.getInstance()
						.setStringSet(AwerySettings.content.GLOBAL_EXCLUDED_TAGS, items)
						.saveAsync();
			}

			default -> Log.e(TAG, "Failed to save tags list");
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
}