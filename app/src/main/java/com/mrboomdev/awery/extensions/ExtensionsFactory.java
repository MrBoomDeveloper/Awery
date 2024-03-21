package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.AweryApp.stream;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.extensions.support.cloudstream.CloudstreamManager;
import com.mrboomdev.awery.extensions.support.js.JsManager;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.extensions.support.yomi.tachiyomi.TachiyomiManager;

import java.util.Collection;
import java.util.List;

import java9.util.stream.StreamSupport;

public class ExtensionsFactory {
	private static final List<ExtensionsManager> managers = List.of(
			new AniyomiManager(), new TachiyomiManager(), new CloudstreamManager(), new JsManager()
	);

	public static void init(@NonNull Context context) {
		for(var extensionManager : managers) {
			extensionManager.initAll(context);
		}

		var failedExtensions = stream(managers)
				.map(manager -> manager.getExtensions(Extension.FLAG_ERROR))
				.flatMap(StreamSupport::stream).toList();

		if(!failedExtensions.isEmpty()) {
			AweryApp.toast("Failed to load " + failedExtensions.size() + " extension(s)");
		}
	}

	@NonNull
	public static Collection<Extension> getExtensions(int flags) {
		return stream(managers)
				.map(manager -> manager.getExtensions(flags))
				.flatMap(StreamSupport::stream).toList();
	}

	public static Collection<ExtensionProvider> getProviders(int flags) {
		return stream(getExtensions(flags))
				.map(Extension::getProviders)
				.flatMap(StreamSupport::stream).toList();
	}
}