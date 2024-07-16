package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.data.Constants.SUPPRESS_IGNORED_THROWABLE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.JsException;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class JsManager extends ExtensionsManager {
	public static final String MANAGER_ID = "AWERY_JS";
	private static final String TAG = "JsManager";
	private final List<Throwable> caughtExceptions = new ArrayList<>();
	private final Map<String, Extension> extensions = new HashMap<>();
	private final Queue<JsTask> tasks = new ConcurrentLinkedDeque<>();
	private org.mozilla.javascript.Context context;

	public JsManager() {
		Thread jsThread = new Thread(() -> {
			this.context = new ContextFactory() {
				@Override
				protected boolean hasFeature(org.mozilla.javascript.Context cx, int featureIndex) {
					// Uhh... Why? Just why?
					if(featureIndex == org.mozilla.javascript.Context.FEATURE_NON_ECMA_GET_YEAR) {
						return true;
					}

					return super.hasFeature(cx, featureIndex);
				}
			}.enterContext();

			this.context.setLanguageVersion(
					org.mozilla.javascript.Context.VERSION_ES6);

			context.setErrorReporter(new ErrorReporter() {
				@Override
				public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
					Log.w("JsManager", lineSource + "\n\"" + message + "\" at " + sourceName + ":" + line);
				}

				@Override
				@SuppressWarnings(SUPPRESS_IGNORED_THROWABLE)
				public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
					Log.e("JsManager", lineSource + "\n\"" + message + "\" at " + sourceName + ":" + line);
					runtimeError(message, sourceName, line, lineSource, lineOffset);
				}

				@NonNull
				@Contract("_, _, _, _, _ -> new")
				@Override
				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
					var e = new EvaluatorException(lineSource + "\n\"" + message + "\" at " + sourceName + ":" + line);
					caughtExceptions.add(e);
					return e;
				}
			});

			// Disable optimization, because it doesn't work on Android
			// Fuck you, DexClassLoaded!
			this.context.setOptimizationLevel(-1);

			while(true) {
				var task = tasks.poll();
				if(task == null) continue;

				try {
					processTask(task);
				} catch(Throwable t) {
					try {
						// Resolve the task with exception
						task.resolve(t);
					} catch(Throwable t1) {
						Log.e(TAG, "Contract broken, stopping the thread! You shouldn't throw any exceptions in the callback!", t1);
						throw t1;
					}
				}
			}
		}, "JsLooper");

		jsThread.start();
	}

	private void processTask(@NonNull JsTask task) {
		switch(task.getTaskType()) {
			case JsTask.LOAD_EXTENSION -> {
				var provider = new JsProvider(this, (Context) task.getArgs()[1], context, (String) task.getArgs()[0]);

				var extension = new Extension(this, provider.id, provider.getName(), provider.version);
				extension.addProvider(provider);
				extension.addFlags(Extension.FLAG_WORKING);
				provider.extension = extension;

				task.resolve(extension);
			}

			case JsTask.LOAD_ALL_EXTENSIONS -> {
				var context = (Context) task.getArgs()[0];
				var dir = new File(context.getFilesDir(), getId());
				var files = dir.listFiles();

				if(!dir.exists() || files == null) {
					task.resolve(true);
					return;
				}

				for(var file : files) {
					var isEnabledKey = "ext_" + getId() + "_" + file.getName() + "_enabled";

					if(!NicePreferences.getPrefs().getBoolean(isEnabledKey, true)) {
						var name = file.getName();
						var extension = new Extension(this, name, name, null);
						extension.setError(Extension.DISABLED_ERROR);

						extensions.put(name, extension);
						continue;
					}

					try(var is = new FileInputStream(file)) {
						var builder = new StringBuilder();

						try(var stream = new BufferedReader(new InputStreamReader(is))) {
							for(String line; (line = stream.readLine()) != null; ) {
								builder.append(line).append("\n");
							}
						}

						var provider = new JsProvider(this,
								(Context) task.getArgs()[0], this.context, builder.toString());

						var extension = new Extension(this,
								provider.id, provider.getName(), provider.version);

						extension.addProvider(provider);
						extension.addFlags(Extension.FLAG_WORKING);
						provider.extension = extension;

						extensions.put(provider.id, extension);
					} catch(IOException e) {
						var name = file.getName();
						var extension = new Extension(this, name, name, "Failed to load!");
						extension.setError(e);
						extensions.put(name, extension);
					} catch(Throwable t) {
						var name = file.getName();
						var extension = new Extension(this, name, name, null);
						extension.setError("Failed to initialize extension", t);
						extensions.put(name, extension);
					}
				}

				task.resolve(true);
			}

			case JsTask.POST_RUNNABLE -> task.resolve(true);
			default -> throw new IllegalArgumentException("Unsupported task type: " + task.getTaskType());
		}
	}

	/**
	 * We have to add a task to the queue, because we can't call it from the main thread,
	 * so please do not touch the Context outside of it's thread!
	 * @author MrBoomDev
	 */
	protected void addTask(JsTask task) {
		tasks.add(task);
	}

	protected Object waitForResult(int taskId, Object... args) {
		var result = new AtomicReference<>();
		tasks.add(new JsTask(taskId, result::set, args));

		Object got;
		while((got = result.get()) == null);
		return got;
	}

	/**
	 * Run action on the JS Thread
	 */
	protected void postRunnable(Runnable action) {
		addTask(new JsTask(action));
	}

	@Override
	public void loadAllExtensions(@NonNull Context context) {
		var result = new AtomicReference<>();
		addTask(new JsTask(JsTask.LOAD_ALL_EXTENSIONS, result::set, context));

		// Wait for the task to finish
		while(result.get() == null);

		if(result.get() instanceof Throwable t) {
			throw new IllegalArgumentException("Failed to load extensions!", t);
		}
	}

	@Override
	public void loadExtension(@NonNull Context context, String id) {
		var file = new File(context.getFilesDir(), getId() + "/" + id);
		var builder = new StringBuilder();

		if(!file.exists()) {
			throw new IllegalArgumentException("Extension file not found!");
		}

		try(var stream = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			for(String line; (line = stream.readLine()) != null;) {
				builder.append(line).append("\n");
			}
		} catch(IOException e) {
			throw new IllegalArgumentException("Failed to read extension from file!", e);
		}

		var result = new AtomicReference<>();
		addTask(new JsTask(JsTask.LOAD_EXTENSION, result::set, builder.toString(), context));

		// Wait for the task to finish
		while(result.get() == null);

		if(result.get() instanceof Throwable t) {
			throw new IllegalArgumentException("Failed to load extension!", t);
		}
	}

	@Override
	public void unloadExtension(Context context, String id) {
		var extension = extensions.get(id);

		if(extension == null) {
			throw new NullPointerException("Extension not found!");
		}

		extension.clearProviders();
	}

	@Override
	public AsyncFuture<Boolean> uninstallExtension(Context context, String id) {
		unloadExtension(context, id);

		var file = new File(context.getFilesDir(), getId() + "/" + id);
		file.delete();

		extensions.remove(id);
		return AsyncUtils.futureNow(true);
	}

	@Override
	public Extension installExtension(Context context, @NonNull InputStream is) throws IOException, JsException {
		var builder = new StringBuilder();

		try(var stream = new BufferedReader(new InputStreamReader(is))) {
			for(String line; (line = stream.readLine()) != null;) {
				builder.append(line).append("\n");
			}
		}

		var script = builder.toString();
		var response = waitForResult(JsTask.LOAD_EXTENSION, script, context);

		if(response instanceof Exception e) {
			if(e instanceof EvaluatorException ex) {
				Log.e(TAG, Constants.LOGS_SEPARATOR);
				throw new JsException(ex, caughtExceptions);
			}

			throw new IllegalArgumentException("Failed to load extension!", e);
		}

		return (Extension) response;
	}

	@Override
	public void addExtension(@NonNull Context context, @NonNull Extension extension) throws IOException {
		String script;

		if(extension.getProviders().get(0) instanceof JsProvider jsProvider) {
			script = jsProvider.script;
		} else {
			throw new IllegalArgumentException("Only Js Extensions are allowed!");
		}

		var file = new File(context.getFilesDir(), getId() + "/" + extension.getId());
		Objects.requireNonNull(file.getParentFile()).mkdirs();
		file.createNewFile();

		try(var output = new BufferedOutputStream(new FileOutputStream(file))) {
			output.write(script.getBytes());
		}

		extensions.put(extension.getId(), extension);
	}

	@Override
	public Extension getExtension(String id) {
		return extensions.get(id);
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return extensions.values();
	}

	@Override
	public MimeTypes[] getExtensionMimeTypes() {
		return new MimeTypes[]{ MimeTypes.JS };
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