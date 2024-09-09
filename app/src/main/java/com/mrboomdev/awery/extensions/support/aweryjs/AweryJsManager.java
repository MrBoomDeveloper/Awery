package com.mrboomdev.awery.extensions.support.aweryjs;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.EMPTY_OBJECT;
import static com.mrboomdev.awery.util.NiceUtils.asRuntimeException;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.async.AsyncUtils.await;
import static com.mrboomdev.awery.util.async.AsyncUtils.controllableEmptyFuture;
import static com.mrboomdev.awery.util.async.AsyncUtils.controllableFuture;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.listFileNames;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.caoccao.javet.exceptions.JavetError;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.Lifecycle;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.Lazy;
import com.mrboomdev.awery.ext.data.Progress;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.async.EmptyFuture;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.io.FileUtil;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class AweryJsManager extends ExtensionsManager {
	public static final String MANAGER_ID = "AWERY_JS";
	private static final String TAG = "AweryJsManager";

	private static final String BRIDGE_KEY = "__AWERY_BINDING__";
	private static final String CONSTANTS_KEY = "__AWERY_CONSTANTS__";
	private static final String GLOBALS_KEY = "__AWERY_GLOBALS__";

	private final List<Runnable> queue = Collections.synchronizedList(new ArrayList<>());
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private final Map<String, __Extension> extensions = new HashMap<>();
	private Thread jsThread;
	private Progress progress;

	/**
	 * This code is being injected at the top of every extension script.
	 */
	public static final String INJECTED_CODE = returnWith(() -> {
		try {
			return FileUtil.readAssets("aweryjs_ext_startup.js");
		} catch(IOException e) {
			throw new IllegalStateException(e);
		}
	});

	/**
	 * Please, don't access this variable before ever accessing the {@link #jsRuntime},
	 * or else it'll throw an {@link JavetException}
	 */
	private final Lazy.Hard<AweryJsConsole, JavetException> globalConsole
			= Lazy.createHardNever(() -> new JavetException(JavetError.LibraryNotLoaded));

	public final Lazy.Hard<V8Runtime, JavetException> jsRuntime = Lazy.createHard(() -> {
		var theRuntime = new AtomicReference<>(EMPTY_OBJECT);

		jsThread = new Thread(() -> {
			try {
				var runtime = V8Host.getV8Instance().createV8Runtime();
				runtime.setGCScheduled(true);
				runtime.allowEval(false);

				/*runtime.getNodeModule(NodeModuleModule.class)
					.setRequireRootDirectory(getRootDir());*/

				var converter = new JavetProxyConverter();
				runtime.setConverter(converter);

				var console = new AweryJsConsole();
				globalConsole.set(console);

				try(var jsConsole = runtime.createV8ValueObject()) {
					jsConsole.bind(console);
					runtime.getGlobalObject().set("console", jsConsole);
				}

				try(var jsConstants = runtime.createV8ValueObject()) {
					jsConstants.bind(new ExtensionConstants());
					runtime.getGlobalObject().set(CONSTANTS_KEY, jsConstants);
				}

				try(var jsGlobalBridge = runtime.createV8ValueObject()) {
					var globalBridge = new AweryJsGlobalBridge();
					jsGlobalBridge.bind(globalBridge);
					runtime.getGlobalObject().set(GLOBALS_KEY, jsGlobalBridge);
				}

				runtime.setPromiseRejectCallback((event, promise, value) -> {
					toast("look at the logcat for some js shit", 1);

					try {
						Log.e(TAG, "Promise has been rejected!", new JsException(value));
					} catch(JavetException e) {
						Log.e(TAG, "Failed to process an V8Value!", e);
					}
				});

				/*runtime.getExecutor("""
						process.on("uncaughtException", (e, origin) => {
							AweryGlobal.toast("look at the logcat for some js shit", 1);
							console.error("An uncaughtException has been raised!", e);
						});
						""").execute().close();*/

				theRuntime.set(runtime);

				while(true) {
					synchronized(queue) {
						var iterator = queue.iterator();

						while(iterator.hasNext()) {
							iterator.next().run();
							iterator.remove();
						}
					}
				}
			} catch(JavetException e) {
				handleError(e);
			}
		}, "AweryJsThread");

		jsThread.setUncaughtExceptionHandler((t, e) -> handleError(e));
		jsThread.start();

		await(() -> theRuntime.get() != EMPTY_OBJECT);
		return (V8Runtime) theRuntime.get();
	});

	private void handleError(Throwable t) {
		CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
				.setPrefix(R.string.please_report_bug_app)
				.setTitle("AweryJS Engine Has Crashed!")
				.setThrowable(t)
				.setDismissCallback(Lifecycle::restartApp)
				.build());
	}

	public EmptyFuture runOnJsThread(AsyncUtils.ThreadEmptyRunnable runnable) {
		try {
			// Check if the runtime was initialized because else our messages would never by ran.
			jsRuntime.get();
		} catch(JavetException e) {
			return AsyncUtils.emptyFutureFailNow(asRuntimeException(e));
		}

		if(Thread.currentThread() == jsThread) {
			try {
				runnable.run();
				return AsyncUtils.futureNow();
			} catch(Throwable e) {
				return AsyncUtils.emptyFutureFailNow(e);
			}
		}

		return controllableEmptyFuture(future -> queue.add(() -> {
			try {
				runnable.run();
				future.complete();
			} catch(Throwable e) {
				future.fail(e);
			}
		}), false);
	}

	public <T> AsyncFuture<T> runOnJsThread(AsyncUtils.ThreadRunnable<T> runnable) {
		if(Thread.currentThread() == jsThread) {
			try {
				return AsyncUtils.futureNow(runnable.run());
			} catch(Throwable e) {
				return AsyncUtils.futureFailNow(e);
			}
		}

		return controllableFuture(future -> queue.add(() -> {
			try {
				future.complete(runnable.run());
			} catch(Throwable e) {
				future.fail(e);
			}
		}), false);
	}

	@NonNull
	@SuppressWarnings("InstantiationOfUtiltyClass")
	private __Extension createExtension(String code) throws JavetException, ExtensionNotInstalledException {
		code = INJECTED_CODE + code;

		var bridge = new AweryJsBridge(this);
		var console = new AweryJsConsole(bridge);

		try(var bridgeJs = jsRuntime.get().createV8ValueObject();
			var consoleJs = jsRuntime.get().createV8ValueObject()
		) {
			bridgeJs.bind(bridge);
			jsRuntime.get().getGlobalObject().set(BRIDGE_KEY, bridgeJs);

			consoleJs.bind(console);
			jsRuntime.get().getGlobalObject().set("console", consoleJs);

			var fun = jsRuntime.get().getExecutor(code).compileV8ValueFunction();
			fun.call(null);
		} finally {
			// We do this to ensure that bad extensions can't access other's data.
			jsRuntime.get().getGlobalObject().delete(BRIDGE_KEY);
			jsRuntime.get().getGlobalObject().set("console", globalConsole.get());
		}

		// Receive manifest data
		if(bridge.jsManifest == null) {
			throw new IllegalStateException("It looks like you've forgot to call an \"setManifest\" method!");
		}

		var minVersion = bridge.jsManifest.getInteger("minVersion");
		var id = requireNonNull(bridge.jsManifest.getString("id"));
		var version = requireNonNull(bridge.jsManifest.getString("version"));
		var title = requireNonNull(bridge.jsManifest.getString("title"));

		if(minVersion != null && minVersion < AweryJsBridge.BRIDGE_VERSION) {
			throw new ExtensionNotInstalledException(title, "Your Awery is too old. Please, update the app to use this extension!");
		}

		var ext = new __Extension(this, id, title, version);
		bridge.extension = ext;

		for(var provider : bridge.providers) {
			provider.extension = ext;
			ext.addProvider(provider);
		}

		return ext;
	}

	@Override
	public AsyncFuture<__Extension> loadExtension(Context context, String id) {
		return thread(() -> {
			try(var is = new FileInputStream(new File(getAnyContext().getFilesDir(), getId() + "/" + id))) {
				var ext = createExtension(FileUtil.readStream(is));
				extensions.put(id, ext);
				return ext;
			} catch(IOException | JavetException e) {
				var extension = new __Extension(this, id, e);

				extensions.put(id, extension);
				getProgress().increment();

				throw new ExtensionNotInstalledException(e);
			}
		});
	}

	@Override
	public AsyncFuture<__Extension> installExtension(Context context, Uri uri) {
		return thread(() -> {
			try(var is = context.getContentResolver().openInputStream(uri)) {
				var read = FileUtil.readStream(is);
				var extension = createExtension(read);

				var file = new File(getAnyContext().getFilesDir(), getId() + "/" + extension.getId());
				FileUtil.createFile(file);

				try(var out = new FileOutputStream(file)) {
					FileUtil.writeStream(out, read.getBytes());
				}

				getProgress().increment();
				getProgress().setMax(getProgress().getMax() + 1);

				extensions.put(extension.getId(), extension);
				return extension;
			}
		});
	}

	@Override
	public AsyncFuture<Boolean> uninstallExtension(Context context, String id) {
		return thread(() -> {
			unloadExtension(id);

			var file = new File(getAnyContext().getFilesDir(), getId() + "/" + id);
			FileUtil.deleteFile(file);

			extensions.remove(id);
			return true;
		});
	}

	@Override
	public EmptyFuture unloadExtension(String id) {
		var extension = extensions.get(id);

		if(extension == null) {
			return AsyncUtils.emptyFutureFailNow(new NoSuchElementException(id));
		}

		extension.clearProviders();
		extension.setError(__Extension.DISABLED_ERROR);
		return AsyncUtils.futureNow();
	}

	@NonNull
	@Contract(" -> new")
	private File getRootDir() {
		return new File(getAnyContext().getFilesDir(), getId());
	}

	@Override
	public EmptyFuture loadAllExtensions(Context context) {
		var files = listFileNames(getRootDir());
		getProgress().setMax(files.length);
		getProgress().setProgress(0);

		return runOnJsThread(() -> {
			for(var name : files) {
				var isEnabledKey = "ext_" + getId() + "_" + name + "_enabled";

				if(!getPrefs().getBoolean(isEnabledKey, true)) {
					extensions.put(name, new __Extension(this, name, __Extension.DISABLED_ERROR));
					continue;
				}

				try {
					loadExtension(context, name).await();
				} catch(ExtensionNotInstalledException e) {
					Log.e(TAG, "Failed to load an extension!", e);
				}
			}
		});
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
	public __Extension getExtension(String id) throws ExtensionNotInstalledException {
		if(!extensions.containsKey(id)) {
			throw new ExtensionNotInstalledException(id);
		}

		return extensions.get(id);
	}

	@Override
	public Collection<__Extension> getAllExtensions() {
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