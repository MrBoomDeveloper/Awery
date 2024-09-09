package com.mrboomdev.awery.extensions.support.aweryjs;

import static com.mrboomdev.awery.app.App.getResourceId;
import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.Lifecycle.getAppContext;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.NiceUtils.serialize;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.annotations.V8Property;
import com.caoccao.javet.enums.V8ValueErrorType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueError;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.activity.LoginActivity;
import com.mrboomdev.awery.util.Lazy;
import com.mrboomdev.awery.util.WeakContext;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpMethod;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.io.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java9.util.stream.Collectors;

@SuppressWarnings("unused")
public class AweryJsBridge {
	protected final List<AweryJsProvider> providers = new ArrayList<>();
	protected V8ValueObject jsManifest;
	protected __Extension extension;
	private static final String TAG = "AweryJsBridge";
	private final AweryJsManager manager;
	protected boolean done;

	private final Lazy.Hard<Storage, JavetException> storage = Lazy.createHard(() ->
			new Storage("EXT_JS_" + extension.getId() + "_" + jsManifest.getString("id")));

	/** Note: Update every time some big changes do happen to the api **/
	protected static final int BRIDGE_VERSION = 1;

	public AweryJsBridge(AweryJsManager manager) {
		this.manager = manager;
	}

	@V8Property
	public Storage getStorage() {
		try {
			return storage.get();
		} catch(JavetException | NullPointerException e) {
			/* Extension hasn't been initialized yet.
			   Usually it crashes at the aweryjs_ext_startup.js Awery object creation while trying to spread this instance.
			   Use an custom getter to somehow work with it. */

			return null;
		}
	}

	@V8Function
	public void setManifest(V8ValueObject manifest) throws JavetException {
		requireArgument(manifest, "manifest");

		if(jsManifest != null) {
			throw new IllegalStateException("You've already set an manifest! It can't be changed!");
		}

		this.jsManifest = manifest.toClone();
	}

	@V8Function
	public void reportError(V8Value value) throws JavetException {
		String title;
		String message;

		if(value instanceof V8ValueError e) {
			title = e.getMessage();
			message = e.getStack();
		} else if(value instanceof V8ValueObject o) {
			title = o.getString("title");
			var content = o.get("error");

			if(content.isNullOrUndefined()) {
				message = "";
			} else if(content instanceof V8ValueError e) {
				message = e.getStack();
			} else {
				message = content.asString();
			}
		} else {
			title = "Error has occurred";
			message = value.asString();
		}

		CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
				.setTitle(title)
				.setMessage(message)
				.build());
	}

	@V8Function
	public V8ValuePromise requestLogin(@NonNull V8ValueObject params) throws JavetException {
		var promise = params.getV8Runtime().createV8ValuePromise();
		var type = params.getString("type");

		switch(type) {
			case "OPEN_BROWSER_REDIRECT" -> {
				var context = getAnyActivity(Activity.class);
				var intent = new Intent(context, LoginActivity.class);
				intent.putExtra(LoginActivity.EXTRA_ACTION, LoginActivity.ACTION_OPEN_BROWSER);
				intent.putExtra(LoginActivity.EXTRA_URL, params.getString("type"));

				startActivityForResult(context, intent, (code, result) -> {
					try {
						if(code == Activity.RESULT_OK) {
							var o = params.getV8Runtime().createV8ValueObject();
							o.set("url", result.getStringExtra(LoginActivity.EXTRA_URL));
							promise.resolve(o);
						} else {
							promise.reject(params.getV8Runtime().createV8ValueError(
									V8ValueErrorType.Error, "CANCELLED"));
						}
					} catch(JavetException e) {
						throw new RuntimeException(e);
					}
				});
			}

			case "INPUT_SCREEN" -> promise.reject(params.getV8Runtime().createV8ValueError(
					V8ValueErrorType.Error, "INPUT_SCREEN isn't implemented yet."));

			default -> promise.reject(params.getV8Runtime().createV8ValueError(
					V8ValueErrorType.Error, "Unknown login type: " + type));
		}

		return promise;
	}

	@V8Function
	public String i18n(String key, String defaultValue) {
		var res = getResourceId(R.string.class, key);

		if(res == 0) {
			return defaultValue;
		}

		return getAppContext().getString(res);
	}

	@V8Property
	public int getBridgeVersion() {
		return BRIDGE_VERSION;
	}

	@V8Function
	public void registerProvider(V8ValueObject provider) throws JavetException {
		requireArgument(provider, "provider");

		if(jsManifest == null) {
			throw new IllegalStateException("You have to set an manifest before registering any providers!");
		}

		if(done) {
			throw new IllegalStateException("You can't add more providers after initialization finished!");
		}

		providers.add(new AweryJsProvider(manager, provider.toClone()));
	}

	@SuppressWarnings("unchecked")
	@V8Function
	public V8ValuePromise fetch(String url, @Nullable Map<String, ?> options) throws JavetException {
		requireArgument(url, "url");

		var promise = manager.jsRuntime.get().createV8ValuePromise();
		var request = new HttpRequest(url);

		if(options != null) {
			if(options.containsKey("method")) {
				var method = (String) options.get("method");
				request.setMethod(HttpMethod.valueOf(method));
			}

			request.setBody((String) options.get("body"));
			request.setForm((Map<String, String>) options.get("form"));
			request.setHeaders((Map<String, String>) options.get("headers"));
		}

		HttpClient.fetch(request).addCallback(new AsyncFuture.Callback<>() {
			@Override
			public void onSuccess(HttpResponse result) {
				try {
					promise.resolve(result);
					//promise.getV8Runtime().await();
				} catch(JavetException e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(@NonNull Throwable t) {
				try {
					var o = manager.jsRuntime.get().createV8ValueObject();
					o.set("id", JsException.OTHER);
					o.set("extra", t.getMessage());

					try {
						o.set(JsException.SERIALIZED_EXCEPTION, serialize(t));
					} catch(IOException e) {
						Log.e(TAG, "Failed to serialize an Throwable!", e);
					}

					promise.reject(o);
					//promise.getV8Runtime().await();
				} catch(JavetException e) {
					throw new RuntimeException(e);
				}
			}
		});

		return promise.getPromise();
	}

	@V8Property
	public String getAdultMode() {
		var mode = AwerySettings.ADULT_MODE.getValue();

		if(mode == null) {
			mode = AwerySettings.AdultMode_Values.SAFE;
		}

		return mode.name();
	}

	public static class Storage {
		private final WeakContext context = new WeakContext();
		private final String fileName;

		public Storage(String fileName) {
			this.fileName = fileName;
		}

		private SharedPreferences getPrefs() {
			return context.get().getSharedPreferences(fileName, 0);
		}

		@V8Function
		public void delete(String key) {
			var prefs = getPrefs().edit().remove(key).commit();
		}

		@SuppressLint("ApplySharedPref")
		@V8Function
		public void set(String key, Object value) {
			if(value == null) {
				delete(key);
				return;
			}

			var prefs = getPrefs().edit();

			if(value instanceof String string) {
				prefs.putString(key, string);
			} else if(value instanceof Integer integer) {
				prefs.putInt(key, integer);
			} else if(value instanceof Long longV) {
				prefs.putLong(key, longV);
			} else if(value instanceof Boolean bool) {
				prefs.putBoolean(key, bool);
			} else if(value instanceof Set<?> set) {
				prefs.putStringSet(key, stream(set)
						.filter(Objects::nonNull)
						.map(Object::toString)
						.collect(Collectors.toSet()));
			} else {
				throw new IllegalArgumentException("Unknown value type! " + value.getClass().getName());
			}

			prefs.commit();
		}

		@V8Function
		public Object get(String key) {
			return getPrefs().getAll().get(key);
		}
	}
}