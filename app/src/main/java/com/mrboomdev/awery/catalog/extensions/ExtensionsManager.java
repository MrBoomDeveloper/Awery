package com.mrboomdev.awery.catalog.extensions;

import android.content.Context;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class ExtensionsManager {

	public Collection<ExtensionProvider> getProviders(int flags) {
		return getExtensions(flags).stream()
				.map(Extension::getProviders)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public Collection<Extension> getExtensions(int flags) {
		return getAllExtensions().stream()
				.filter(extension -> (flags & extension.getFlags()) != 0)
				.collect(Collectors.toList());
	}

	public abstract Extension getExtension(String id);

	public abstract Collection<Extension> getAllExtensions();

	public abstract String getName();

	public abstract String getId();

	public void initAll(Context context) {}

	public void init(Context context, String id) {}

	public void unload(Context context, String id) {}

	public void unloadAll(Context context) {}
}