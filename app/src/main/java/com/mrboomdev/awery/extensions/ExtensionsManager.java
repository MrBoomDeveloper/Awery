package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.content.Context;

import com.mrboomdev.awery.util.MimeTypes;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.io.IOException;
import java.io.InputStream;
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

	/**
	 * Find an extension by a unique id
	 * @author MrBoomDev
	 */
	public abstract Extension getExtension(String id);

	public abstract Collection<Extension> getAllExtensions();

	public boolean hasExtension(String id) {
		return getExtension(id) != null;
	}

	public abstract String getName();

	/**
	 * @return A unique id of the extension
	 * @author MrBoomDev
	 */
	public abstract String getId();

	public MimeTypes[] getExtensionMimeTypes() {
		return new MimeTypes[]{ MimeTypes.ANY };
	}

	public Extension installExtension(Context context, InputStream stream) throws IOException {
		throw new UnimplementedException("This extension manager doesn't support installing extensions");
	}

	public void addExtension(Context context, Extension extension) throws IOException {
		throw new UnimplementedException("This extension manager doesn't support adding extensions");
	}

	public void loadAllExtensions(Context context) {
		for(var extension : getAllExtensions()) {
			loadExtension(context, extension.getId());
		}
	}

	public void loadExtension(Context context, String id) {}

	/**
	 * Called after a user has disabled the extension in settings or app is closing
	 * @author MrBoomDev
	 */
	public void unloadExtension(Context context, String id) {}

	public void uninstallExtension(Context context, String id) {
		unloadExtension(context, id);
	}

	/**
	 * Usually called once the app is closing
	 * @author MrBoomDev
	 */
	public void unloadAllExtensions(Context context) {
		for(var extension : getAllExtensions()) {
			unloadExtension(context, extension.getId());
		}
	}
}