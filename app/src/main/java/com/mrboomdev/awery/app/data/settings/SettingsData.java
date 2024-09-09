package com.mrboomdev.awery.app.data.settings;

import static com.mrboomdev.awery.app.Lifecycle.getAppContext;
import static com.mrboomdev.awery.util.NiceUtils.formatFileSize;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.getFileSize;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.LocaleListCompat;

import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.data.Constants;
import com.mrboomdev.awery.app.data.settings.base.LazySettingsItem;
import com.mrboomdev.awery.ext.data.Selection;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.ext.source.ExtensionsManager;
import com.mrboomdev.awery.extensions.ExtensionSettings;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.ui.activity.settings.TabsSettings;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;

@Deprecated(forRemoval = true)
public class SettingsData {

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

				callback.onError(new UnsupportedOperationException("Will be available later..."));
			}

			default -> callback.onResult(null, new IllegalArgumentException("Failed to load tags list"));
		}
	}

	@NonNull
	@Contract(pure = true)
	public static AsyncFuture<Setting> getScreen(AppCompatActivity activity, @NonNull Setting item) {
		if(item instanceof LazySettingsItem lazySettingsItem) {
			return lazySettingsItem.loadLazily();
		}

		return thread(() -> {
			var behaviourId = item.getExtra();

			if(behaviourId != null) {
				if(behaviourId.startsWith("extensions_")) {
					var factory = ExtensionsFactory.getInstance().await();

					var manager = factory.getManager((Class<? extends ExtensionsManager>) switch(behaviourId) {
						//case "extensions_aweryjs" -> AweryJsManager.class;
						//case "extensions_miru" -> MiruManager.class;
						//case "extensions_cloudstream" -> CloudstreamManager.class;
						case "extensions_aniyomi" -> AniyomiManager.class;
						//case "extensions_tachiyomi" -> TachiyomiManager.class;
						default -> throw new IllegalArgumentException("Unknown extension manager! " + behaviourId);
					});

					return new ExtensionSettings(activity, manager);
				}

				return switch(behaviourId) {
					case "tabs" -> {
						var screen = new TabsSettings();
						screen.loadData();
						yield screen;
					}

					default -> throw new IllegalArgumentException("Unknown screen: " + behaviourId);
				};
			}

			throw new ZeroResultsException("Can't find any items", R.string.no);
		});
	}
}