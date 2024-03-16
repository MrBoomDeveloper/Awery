package com.mrboomdev.awery.catalog.extensions;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.catalog.extensions.support.cloudstream.CloudstreamManager;
import com.mrboomdev.awery.catalog.extensions.support.js.JsManager;
import com.mrboomdev.awery.catalog.extensions.support.yomi.tachiyomi.TachiyomiManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExtensionsFactory {
	private static final List<ExtensionsManager> managers = List.of(
			new AniyomiManager(), new TachiyomiManager(), new CloudstreamManager(), new JsManager()
	);

	public static void init(@NonNull Context context) {
		for(var extensionManager : managers) {
			extensionManager.initAll(context);
		}

		var failedExtensions = managers.stream()
				.map(manager -> manager.getExtensions(Extension.FLAG_ERROR))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		if(!failedExtensions.isEmpty()) {
			AweryApp.toast("Failed to load " + failedExtensions.size() + " extension(s)");
		}
	}

	@NonNull
	public static Collection<Extension> getExtensions(int flags) {
		return managers.stream()
				.map(manager -> manager.getExtensions(flags))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public static Collection<ExtensionProvider> getProviders(int flags) {
		return getExtensions(flags).stream()
				.map(Extension::getProviders)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}