package com.mrboomdev.awery.extensions.support.js;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.util.MimeTypes;

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
	private final Map<String, Extension> extensions = new HashMap<>();
	private final Queue<JsTask> tasks = new ConcurrentLinkedDeque<>();
	private org.mozilla.javascript.Context context;

	public JsManager() {
		new Thread(() -> {
			this.context = org.mozilla.javascript.Context.enter();

			// Disable optimization, because it doesn't work on Android
			// Fuck you, DexClassLoaded!
			this.context.setOptimizationLevel(-1);

			while(true) {
				var task = tasks.poll();
				if(task == null) continue;

				try {
					switch(task.getTaskType()) {
						case JsTask.LOAD_EXTENSION -> {
							var provider = new JsProvider(this, context, (String) task.getExtra());
							var extension = new Extension(this, provider.id, provider.getName(), provider.version);
							extensions.put(provider.id, extension);
							task.getCallback().run(provider.id);
						}

						case JsTask.LOAD_ALL_EXTENSIONS -> {
							var context = (Context) task.getExtra();
							var dir = new File(context.getFilesDir(), getId());
							var files = dir.listFiles();
							if(!dir.exists() || files == null) return;

							for(var file : files) {
								var isEnabledKey = "ext_" + getId() + "_"
										+ getExtensionIdFromFileName(file.getName()) + "_enabled";

								if(!AwerySettings.getInstance().getBoolean(isEnabledKey, true)) {
									var name = getExtensionIdFromFileName(file.getName());
									var extension = new Extension(this, name, name, "Disabled.");
									extensions.put(name, extension);
									continue;
								}

								try(var is = new FileInputStream(file)) {
									var builder = new StringBuilder();

									try(var stream = new BufferedReader(new InputStreamReader(is))) {
										for(String line; (line = stream.readLine()) != null;) {
											builder.append(line);
										}
									}

									var provider = new JsProvider(this,
											this.context, builder.toString());

									var extension = new Extension(this,
											provider.id, provider.getName(), provider.version);

									extension.addProvider(provider);
									extensions.put(provider.id, extension);
								} catch(IOException e) {
									var name = getExtensionIdFromFileName(file.getName());
									var extension = new Extension(this, name, name, "Failed to load!");
									extension.setError(e);
									extensions.put(name, extension);
								}
							}

							task.getCallback().run(true);
						 }

						default -> throw new IllegalArgumentException("Unsupported task type: " + task.getTaskType());
					}
				} catch(Throwable t) {
					task.getCallback().run(t);
				}
			}
		}, "JsManager").start();
	}

	protected void addTask(JsTask task) {
		tasks.add(task);
	}

	@Override
	public void initAll(@NonNull Context context) {
		var result = new AtomicReference<>();
		addTask(new JsTask(JsTask.LOAD_ALL_EXTENSIONS, context, result::set));

		// Wait for the task to finish
		while(result.get() == null);

		if(result.get() instanceof Throwable t) {
			throw new IllegalArgumentException("Failed to load extensions!", t);
		}
	}

	@NonNull
	private String getExtensionIdFromFileName(@NonNull String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	@Override
	public void init(@NonNull Context context, String id) {
		var file = new File(context.getFilesDir(), getId() + "/" +id + ".js");
		var builder = new StringBuilder();

		if(!file.exists()) {
			throw new IllegalArgumentException("Extension file not found!");
		}

		try(var stream = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			for(String line; (line = stream.readLine()) != null;) {
				builder.append(line);
			}
		} catch(IOException e) {
			throw new IllegalArgumentException("Failed to read extension from file!", e);
		}

		var result = new AtomicReference<>();
		tasks.add(new JsTask(JsTask.LOAD_EXTENSION, builder.toString(), result::set));

		// Wait for the task to finish
		while(result.get() == null);

		if(result.get() instanceof Throwable t) {
			throw new IllegalArgumentException("Failed to load extension!", t);
		}
	}

	@Override
	public void unload(Context context, String id) {
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
				builder.append(line);
			}
		}

		var result = new AtomicReference<>();
		var task = new JsTask(JsTask.LOAD_EXTENSION, builder.toString(), result::set);
		addTask(task);

		// Wait for the task to finish
		while(result.get() == null);

		if(result.get() instanceof Exception e) {
			throw new IllegalArgumentException("Failed to load extension!", e);
		}

		var file = new File(context.getFilesDir(), getId() + "/" + result.get() + ".js");
		Objects.requireNonNull(file.getParentFile()).mkdirs();
		file.createNewFile();

		try(var output = new BufferedOutputStream(new FileOutputStream(file))) {
			output.write(builder.toString().getBytes());
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