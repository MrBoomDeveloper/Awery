package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.extensions.support.anilist.AnilistProvider;
import com.mrboomdev.awery.extensions.support.js.JsManager;
import com.mrboomdev.awery.extensions.support.yomi.YomiHelper;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiProvider;
import com.mrboomdev.awery.util.NiceUtils;

import java.util.Collection;
import java.util.List;

import java9.util.Objects;
import java9.util.stream.StreamSupport;

public class ExtensionsFactory {
	private static final String TAG = "ExtensionsFactory";
	private static final List<ExtensionsManager> managers = List.of(
			new AniyomiManager(),
			//new TachiyomiManager(), // We doesn't support manga reading at the moment so we can just disable it for now
			//new CloudstreamManager(),
			//new MiruManager(),
			new JsManager());

	public static void init(@NonNull Application context) {
		YomiHelper.init(context);

		for(var manager : managers) {
			manager.loadAllExtensions(context);
		}

		var failedExtensions = stream(managers)
				.map(manager -> manager.getExtensions(Extension.FLAG_ERROR))
				.flatMap(NiceUtils::stream)
				.filter(extension -> !Objects.equals(extension.getErrorTitle(), Extension.DISABLED_ERROR))
				.toList();

		if(!failedExtensions.isEmpty()) {
			Log.e(TAG, "");
			Log.e(TAG, Constants.LOGS_SEPARATOR);

			for(var extension : failedExtensions) {
				if(extension.getError() != null) Log.e(TAG, extension.getErrorTitle(), extension.getError());
				else Log.e(TAG, extension.getErrorTitle());

				Log.e(TAG, Constants.LOGS_SEPARATOR);
			}

			var text = "Failed to load " + failedExtensions.size() + " extension(s)";

			Log.e(TAG, "");
			Log.e(TAG, text);
			AweryApp.toast(text);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends ExtensionsManager> T getManager(Class<T> clazz) {
		return (T) stream(managers)
				.filter(manager -> manager.getClass() == clazz)
				.findFirst().orElseThrow();
	}

	public static ExtensionsManager getManager(@NonNull String name) {
		return getManager((Class<? extends ExtensionsManager>) switch(name) {
			case AniyomiManager.MANAGER_ID -> AniyomiManager.class;
			case JsManager.MANAGER_ID -> JsManager.class;
			default -> throw new IllegalArgumentException("Extensions manager \"" + name + " \" was not found!");
		});
	}

	@Nullable
	public static ExtensionProvider getExtensionProvider(int extensionFlags, @NonNull String id) {
		if(id.contains(";;;")) {
			var parts = id.split(";;;");

			// TODO: When js extensions will be fully done remove this shit from the code
			if(parts[0].equals(JsManager.MANAGER_ID) && parts[1].equals(Constants.ANILIST_EXTENSION_ID)) {
				return AnilistProvider.getInstance();
			}

			var extension = stream(managers)
					.filter(manager -> manager.getId().equals(parts[0]))
					.findAny().orElseThrow()
					.getExtension(parts[1]);

			if(extension == null) {
				return null;
			}

			return extension.getProviders().get(0);
		}

		return stream(getExtensions(extensionFlags))
				.map(Extension::getProviders)
				.flatMap(NiceUtils::stream)
				.filter(provider -> id.equals(provider.getId()))
				.findAny().orElse(null);
	}

	@NonNull
	public static Collection<Extension> getExtensions(int flags) {
		return stream(managers)
				.map(manager -> manager.getExtensions(flags))
				.flatMap(StreamSupport::stream).toList();
	}
}