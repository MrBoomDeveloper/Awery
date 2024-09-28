package com.mrboomdev.awery.extensions.support.aweryjs;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.listFileNames;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.util.Progress;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class AweryJsManager extends ExtensionsManager {
	public static final String MANAGER_ID = "AWERY_JS";
	private static final String TAG = "AweryJsManager";
	private final Map<String, Extension> extensions = new HashMap<>();
	/**
	 * DO NOT TOUCH DIRECTLY OUTSIDE OF {@link #getJsRuntime()}
	 */
	private V8Runtime runtime;
	private Progress progress;

	@NonNull
	private Extension createExtension(String code) throws JavetException {
		AweryJsBridge bridge;

		try(var o = getJsRuntime().createV8ValueObject()) {
			bridge = new AweryJsBridge(this);
			runtime.getGlobalObject().set("Awery", o);
			o.bind(bridge);
		}

		try(var script = getJsRuntime().getExecutor(code).compileV8Script()) {
			script.executeVoid();

			if(bridge.jsManifest == null) {
				throw new IllegalStateException("It looks like you've forgot to call the \"setManifest\"!");
			}

			var ext = new Extension(this,
					(String) bridge.jsManifest.get("id"),
					(String) bridge.jsManifest.get("title"),
					(String) bridge.jsManifest.get("version"));

			for(var provider : bridge.providers) {
				ext.addProvider(provider);
			}

			return ext;
		} finally {
			bridge.jsManifest = null;
			bridge.done = true;
			bridge.providers.clear();
		}
	}

	@Override
	public void loadExtension(Context context, String id) {
		var file = new File(getAnyContext().getFilesDir(), getId() + "/" + id);
		Extension extension;

		try(var is = new FileInputStream(file)) {
			extension = createExtension(FileUtil.readStream(is));
		} catch(IOException | JavetException e) {
			Log.e(TAG, "Failed to load an extension!", e);
			extension = new Extension(this, id, e);
		}

		extensions.put(id, extension);
		getProgress().increment();
	}

	@Override
	public AsyncFuture<Extension> installExtension(Context context, Uri uri) {
		return thread(() -> {
			try(var is = context.getContentResolver().openInputStream(uri)) {
				var read = FileUtil.readStream(is);
				var extension = createExtension(read);

				var file = new File(getAnyContext().getFilesDir(), getId() + "/" + extension.getId());
				FileUtil.createFile(file);

				try(var out = new FileOutputStream(file)) {
					FileUtil.writeStream(out, read.getBytes());
				}

				extensions.put(extension.getId(), extension);
				return extension;
			}
		});
	}

	@Override
	public AsyncFuture<Boolean> uninstallExtension(Context context, String id) {
		return thread(() -> {
			unloadExtension(context, id);

			var file = new File(getAnyContext().getFilesDir(), getId() + "/" + id);
			FileUtil.deleteFile(file);

			extensions.remove(id);
			return true;
		});
	}

	@Override
	public void unloadExtension(Context context, String id) {
		var extension = extensions.get(id);

		if(extension == null) {
			throw new NoSuchElementException();
		}

		extension.clearProviders();
		extension.setError(Extension.DISABLED_ERROR);
	}

	public V8Runtime getJsRuntime(boolean init) throws JavetException {
		return init ? getJsRuntime() : runtime;
	}

	public V8Runtime getJsRuntime() throws JavetException {
		if(runtime != null) {
			return runtime;
		}

		runtime = V8Host.getNodeInstance().createV8Runtime();
		runtime.setGCScheduled(true);
		runtime.setConverter(new JavetProxyConverter());
		runtime.allowEval(false);

		var console = new JavetStandardConsoleInterceptor(runtime);
		console.register(runtime.getGlobalObject());

		return runtime;
	}

	@Override
	public void loadAllExtensions(Context context) {
		for(var name : listFileNames(new File(getAnyContext().getFilesDir(), getId()))) {
			var isEnabledKey = "ext_" + getId() + "_" + name + "_enabled";

			if(!getPrefs().getBoolean(isEnabledKey, true)) {
				extensions.put(name, new Extension(this, name, Extension.DISABLED_ERROR));
				continue;
			}

			loadExtension(context, name);
		}
	}

	@Override
	public Progress getProgress() {
		if(progress == null) {
			var rootDirectory = new File(getAnyContext().getFilesDir(), getId());
			progress = new Progress(listFileNames(rootDirectory).length);
		}

		return progress;
	}

	@Override
	public Extension getExtension(String id) throws ExtensionNotInstalledException {
		if(!extensions.containsKey(id)) {
			throw new ExtensionNotInstalledException(id);
		}

		return extensions.get(id);
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return extensions.values();
	}

	@Override
	public String getName() {
		return "AweryJS";
	}

	@Override
	public String getId() {
		return MANAGER_ID;
	}
}