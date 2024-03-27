package com.mrboomdev.awery.extensions.support.js;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.util.MimeTypes;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class JsManager extends ExtensionsManager {
	private static final String TAG = "JsManager";
	private final Map<String, Extension> extensions = new HashMap<>();
	private final Queue<JsTask> tasks = new ConcurrentLinkedDeque<>();
	private org.mozilla.javascript.Context context;

	public JsManager() {
		// Disable optimization, because it doesn't work on Android
		// Fuck you, DexClassLoaded!
		Thread jsThread = new Thread(() -> {
			this.context = org.mozilla.javascript.Context.enter();

			context.setErrorReporter(new ErrorReporter() {
				@Override
				public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
					Log.w("JsManager", lineSource + "\n\"" + message + "\" at " + sourceName + ":" + line);
				}

				@Override
				public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
					Log.e("JsManager", lineSource + "\n\"" + message + "\" at " + sourceName + ":" + line);
				}

				@Override
				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
					return new EvaluatorException(lineSource + "\n\"" + message + "\" at " + sourceName + ":" + line);
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
					task.getCallback().run(t);
				}
			}
		}, "JsLooper");

		jsThread.setUncaughtExceptionHandler((t, e) -> {
			// Something very horrible happened
			Log.e("JsManager", "JsLoop crashed!", e);
			System.exit(-1);
		});

		jsThread.start();
	}

	private void processTask(@NonNull JsTask task) {
		switch(task.getTaskType()) {
			case JsTask.LOAD_EXTENSION -> {
				var provider = new JsProvider(this, context, (String) task.getArgs()[0], (String) task.getArgs()[1]);
				var extension = new Extension(this, provider.id, provider.getName(), provider.version);
				extensions.put(provider.id, extension);
				task.getCallback().run(provider.id);
			}

			case JsTask.LOAD_ALL_EXTENSIONS -> {
				var context = (Context) task.getArgs()[0];
				var dir = new File(context.getFilesDir(), getId());
				var files = dir.listFiles();

				if(!dir.exists() || files == null) {
					task.getCallback().run(true);
					return;
				}

				for(var file : files) {
					var isEnabledKey = "ext_" + getId() + "_" + file.getName() + "_enabled";

					if(!AwerySettings.getInstance().getBoolean(isEnabledKey, true)) {
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
								this.context, builder.toString(), file.getName());

						var extension = new Extension(this,
								provider.id, provider.getName(), provider.version);

						extension.addProvider(provider);
						extensions.put(provider.id, extension);
					} catch(IOException e) {
						var name = file.getName();
						var extension = new Extension(this, name, name, "Failed to load!");
						extension.setError(e);
						extensions.put(name, extension);
					}
				}

				task.getCallback().run(true);
			}

			case JsTask.POST_RUNNABLE -> task.getCallback().run(true);
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
		addTask(new JsTask(JsTask.LOAD_EXTENSION, result::set, builder.toString(), file.getName()));

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
	public void installExtension(Context context, @NonNull InputStream is) throws IOException {
		var builder = new StringBuilder();

		try(var stream = new BufferedReader(new InputStreamReader(is))) {
			for(String line; (line = stream.readLine()) != null;) {
				builder.append(line).append("\n");
			}
		}

		var script = builder.toString();
		var response = waitForResult(JsTask.LOAD_EXTENSION, script, "Unknown");

		if(response instanceof Exception e) {
			throw new IllegalArgumentException("Failed to load extension!", e);
		}

		var file = new File(context.getFilesDir(), getId() + "/" + response);
		Objects.requireNonNull(file.getParentFile()).mkdirs();
		file.createNewFile();

		try(var output = new BufferedOutputStream(new FileOutputStream(file))) {
			output.write(script.getBytes());
		}
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
		return "AWERY_JS";
	}
}