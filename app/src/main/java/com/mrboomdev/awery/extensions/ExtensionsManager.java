package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.AweryApp.stream;

import android.content.Context;

import java.util.Collection;

import java9.util.stream.StreamSupport;

public abstract class ExtensionsManager {

	public Collection<ExtensionProvider> getProviders(int flags) {
		return stream(getExtensions(flags))
				.map(Extension::getProviders)
				.flatMap(StreamSupport::stream).toList();
	}

	public Collection<Extension> getExtensions(int flags) {
		return stream(getAllExtensions())
				.filter(extension -> (extension.getFlags() & flags) == flags)
				.toList();
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