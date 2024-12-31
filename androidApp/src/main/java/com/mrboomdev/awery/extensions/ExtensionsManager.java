package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.net.Uri;

import com.mrboomdev.awery.ext.util.Progress;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;

import java.util.Collection;
import java.util.List;

import kotlin.NotImplementedError;

public abstract class ExtensionsManager {
	private final Progress progress = new Progress();

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

	public abstract String getName();

	/**
	 * @return A unique id of the extension
	 * @author MrBoomDev
	 */
	public abstract String getId();

	public AsyncFuture<Extension> installExtension(Context context, Uri uri) {
		return AsyncUtils.futureFailNow(new NotImplementedError("This extension manager doesn't support installing extensions"));
	}

	public void loadAllExtensions(Context context) {
		for(var extension : getAllExtensions()) {
			loadExtension(context, extension.getId());
		}
	}

	public Progress getProgress() {
		return progress;
	}

	public void loadExtension(Context context, String id) {}

	/**
	 * Called after a user has disabled the extension in settings or app is closing
	 * @author MrBoomDev
	 */
	public void unloadExtension(Context context, String id) {}

	public AsyncFuture<Boolean> uninstallExtension(Context context, String id) {
		return AsyncUtils.futureFailNow(new NotImplementedError("This type of extensions cannot be uninstalled currently"));
	}

	public AsyncFuture<List<Extension>> getRepository(String url) {
		return AsyncUtils.futureFailNow(new NotImplementedError("This extension manager do not support repositories!"));
	}
}