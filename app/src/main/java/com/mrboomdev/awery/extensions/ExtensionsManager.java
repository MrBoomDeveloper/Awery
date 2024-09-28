package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.net.Uri;

import com.mrboomdev.awery.util.Progress;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Collection;
import java.util.List;

public abstract class ExtensionsManager {

	public Collection<? extends Extension> getExtensions(int flags) {
		return stream(getAllExtensions())
				.filter(extension -> (extension.getFlags() & flags) == flags)
				.toList();
	}

	/**
	 * Find an extension by a unique id
	 * @author MrBoomDev
	 */
	public abstract Extension getExtension(String id) throws ExtensionNotInstalledException;

	public abstract Collection<? extends Extension> getAllExtensions();

	public boolean hasExtension(String id) {
		try {
			return getExtension(id) != null;
		} catch(ExtensionNotInstalledException e) {
			return false;
		}
	}

	public abstract String getName();

	/**
	 * @return A unique id of the extension
	 * @author MrBoomDev
	 */
	public abstract String getId();

	public AsyncFuture<Extension> installExtension(Context context, Uri uri) {
		return AsyncUtils.futureFailNow(new UnimplementedException("This extension manager doesn't support installing extensions"));
	}

	public void loadAllExtensions(Context context) {
		for(var extension : getAllExtensions()) {
			loadExtension(context, extension.getId());
		}
	}

	public Progress getProgress() {
		return Progress.EMPTY;
	}

	public void loadExtension(Context context, String id) {}

	/**
	 * Called after a user has disabled the extension in settings or app is closing
	 * @author MrBoomDev
	 */
	public void unloadExtension(Context context, String id) {}

	public AsyncFuture<Boolean> uninstallExtension(Context context, String id) {
		return AsyncUtils.futureFailNow(new UnimplementedException("This type of extensions cannot be uninstalled currently"));
	}

	public AsyncFuture<List<Extension>> getRepository(String url) {
		return AsyncUtils.futureFailNow(new UnimplementedException("This extension manager do not support repositories!"));
	}
}