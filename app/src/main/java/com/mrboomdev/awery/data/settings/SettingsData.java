package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.app.AweryLifecycle.getAppContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.formatFileSize;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.getFileSize;

import android.content.Context;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.extensions.ExtensionSettings;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.extensions.support.yomi.tachiyomi.TachiyomiManager;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.mobile.screens.settings.TabsSettings;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;

import java9.util.stream.Collectors;
import kotlin.NotImplementedError;

public class SettingsData {
	private static final String TAG = "SettingsData";

	public static String resolveValue(@NonNull String key) {
		return switch(key) {
			case "APP_VERSION" -> BuildConfig.VERSION_NAME;

			case "IMAGE_CACHE_SIZE" -> formatFileSize(getFileSize(new File(
					getAppContext().getCacheDir(), Constants.DIRECTORY_IMAGE_CACHE)));

			case "NET_CACHE_SIZE" -> formatFileSize(getFileSize(new File(
					getAppContext().getCacheDir(), Constants.DIRECTORY_NET_CACHE)));

			case "WEBVIEW_CACHE_SIZE" -> formatFileSize(getFileSize(new File(
					getAppContext().getCacheDir(), Constants.DIRECTORY_WEBVIEW_CACHE)));

			default -> throw new IllegalArgumentException(key + " was not found!");
		};
	}

	public static void getSelectionList(
			Context context,
			@NonNull String listId,
			Errorable<Selection<Selection.Selectable<String>>, Throwable> callback
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
				/*var flags = switch(AwerySettings.ADULT_MODE.getValue()) {
					case DISABLED -> AnilistTagsQuery.SAFE;
					case ENABLED -> AnilistTagsQuery.ALL;
					case ONLY -> AnilistTagsQuery.ADULT;
				};

				AnilistTagsQuery.getTags(flags).executeQuery(context, tags -> {
					var excluded = NicePreferences.getPrefs().getStringSet(AwerySettings.GLOBAL_EXCLUDED_TAGS);

					callback.onResult(stream(tags).map(tag -> {
						var state = excluded.contains(tag.getName()) ?
								Selection.State.SELECTED :
								Selection.State.UNSELECTED;

						return new Selection.Selectable<>(tag.getName(), tag.getName(), state);
					}).collect(Selection.collect()), null);
				}).catchExceptions(e -> callback.onResult(null, e));*/

				callback.onError(new NotImplementedError("Will be available later..."));
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

				NicePreferences.getPrefs()
						.setStringSet(AwerySettings.GLOBAL_EXCLUDED_TAGS, items)
						.saveAsync();
			}

			default -> Log.e(TAG, "Failed to save tags list");
		}
	}
	
	public interface Errorable<T, E extends Throwable> {
		void onResult(T t, E e);
		
		default void onSuccess(T t) {
			onResult(t, null);
		}
		
		default void onError(E e) {
			onResult(null, e);
		}
	}
	
	@Contract(pure = true)
	public static void getScreen(
			AppCompatActivity activity,
			@NonNull SettingsItem item,
			@MainThread Errorable<SettingsItem, Throwable> callback
	) {
		var behaviourId = item.getBehaviour();

		if(behaviourId != null) {
			if(behaviourId.startsWith("extensions_")) {
				var manager = ExtensionsFactory.getManager__Deprecated((Class<? extends ExtensionsManager>) switch(behaviourId) {
					case "extensions_aniyomi" -> AniyomiManager.class;
					case "extensions_tachiyomi" -> TachiyomiManager.class;
					default -> throw new IllegalArgumentException("Unknown extension manager! " + behaviourId);
				});

				var screen = new ExtensionSettings(activity, manager);

				thread(() -> {
					screen.loadData();
					runOnUiThread(() -> callback.onResult(screen, null));
				});
			} else {
				switch(behaviourId) {
					case "tabs" -> thread(() -> {
						var screen = new TabsSettings();
						screen.loadData();
						runOnUiThread(() -> callback.onResult(screen, null));
					});

					default -> callback.onResult(null,
							new IllegalArgumentException("Unknown screen: " + behaviourId));
				}
			}
		} else if(item instanceof LazySettingsItem lazySettingsItem) {
			lazySettingsItem.loadLazily().addCallback(new AsyncFuture.Callback<>() {
				@Override
				public void onSuccess(SettingsItem result) {
					runOnUiThread(() -> callback.onSuccess(result));
				}

				@Override
				public void onFailure(@NonNull Throwable t) {
					runOnUiThread(() -> callback.onError(t));
				}
			});
		} else {
			callback.onError(new ZeroResultsException("Can't find any items", R.string.no));
		}
	}
}