package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public abstract class ExtensionsManager {

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

	public void getRepository(String url, @NonNull Callbacks.Errorable<List<Extension>, Throwable> callback) {
		callback.onResult(null, new UnimplementedException("This extension manager do not support repositories!"));
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