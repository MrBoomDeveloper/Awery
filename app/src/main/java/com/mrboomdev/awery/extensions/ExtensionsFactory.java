package com.mrboomdev.awery.extensions;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.await;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.ext.source.ExtensionsManager;
import com.mrboomdev.awery.ext.data.Progress;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtensionsFactory {
	private static final String TAG = "ExtensionsFactory";
	private static ExtensionsFactory instance;
	private static AsyncFuture<ExtensionsFactory> pendingFuture;
	protected static final Progress progress = new Progress();
	private final Set<ExtensionsManager> managers = new HashSet<>();

	/**
	 * This method will not try load the ExtensionsFactory, so use it only if you know why you want to.
	 * @author MrBoomDev
	 */
	@Nullable
	public static ExtensionsFactory getInstanceNow() {
		return instance;
	}

	public Set<ExtensionsManager> getManagers() {
		return managers;
	}

	@NonNull
	public static AsyncFuture<ExtensionsFactory> getInstance() {
		if(instance != null) {
			return AsyncUtils.futureNow(instance);
		}

		if(pendingFuture != null) {
			return pendingFuture;
		}

		return pendingFuture = thread(() -> {
			var instance = new ExtensionsFactory();
			pendingFuture = null;
			return instance;
		});
	}

	private ExtensionsFactory() {
		Log.d(TAG, "Start loading...");
		instance = this;

		registerManager(new AniyomiManager());
		//registerManager(new TachiyomiManager()); // We doesn't support manga reading at the moment so we can just disable it for now
		//registerManager(new CloudstreamManager());
		//registerManager(new MiruManager());
		//registerManager(new AweryJsManager());
		//registerManager(new InternalManager());

		var finishedManagers = new AtomicInteger();

		for(var manager : managers) {
			thread(() -> {
				manager.loadAllExtensions();
				finishedManagers.addAndGet(1);
			});
		}

		await(() -> finishedManagers.get() == managers.size());

		var failedExtensions = stream(managers)
				.map(ExtensionsManager::getAllExtensions)
				.flatMap(NiceUtils::stream)
				.filter(ext -> ext.getError() == null)
				.toList();

		if(!failedExtensions.isEmpty()) {
			var text = "Failed to load " + failedExtensions.size() + " extension(s)";
			Log.e(TAG, text);
			toast(text);
		}

		Log.d(TAG, "Finished loading");
	}

	public void registerManager(ExtensionsManager manager) {
		managers.add(manager);
	}

	public void unregisterManager(ExtensionsManager manager) {
		managers.remove(manager);
	}

	@SuppressWarnings("unchecked")
	public <T extends ExtensionsManager> T getManager(Class<T> clazz) throws NoSuchElementException {
		return (T) stream(managers)
				.filter(manager -> manager.getClass() == clazz)
				.findFirst().orElseThrow();
	}

	@NonNull
	public ExtensionsManager getManager(@NonNull String name) throws NoSuchElementException {
		return stream(managers)
				.filter(manager -> manager.getId().equals(name))
				.findFirst().orElseThrow();
	}
}