package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryApp.getAnyContext;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.util.MimeTypes;
import com.mrboomdev.awery.util.io.HttpClient;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Do not ever init this class by yourself!
 * @author MrBoomDev
 */
@SuppressWarnings("unused")
public class JsBridge {
	protected AwerySettings prefs;
	private final JsManager manager;
	private final JsProvider provider;
	private final Scriptable scriptScope;
	private Scriptable prototype, parent;

	public JsBridge(JsManager manager, JsProvider provider, Scriptable scope) {
		this.manager = manager;
		this.provider = provider;
		this.scriptScope = scope;
	}

	/**
	 * Must be called right after script was parsed!
	 * @author MrBoomDev
	 */
	public void setManifest(ScriptableObject object) {
		provider.finishInit(this, object);
	}

	public void toast(Object object) {
		AweryApp.toast(object);
	}

	public Object fetch(@NonNull ScriptableObject options) {
		var context = getAnyContext();
		var promise = new JsPromise(scriptScope);

		Map<String, String> headers = null;

		if(options.get("headers") instanceof NativeObject o) {
			headers = new HashMap<>();

			for(var entry : o.entrySet()) {
				headers.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}

		var request = new HttpClient.Request()
				.setHeaders(headers)
				.setUrl(options.get("url").toString());

		if(options.has("body", options)) {
			request.setBody(options.get("body").toString(), MimeTypes.ANY);
		}

		if(options.has("method", options)) {
			var method = options.get("method").toString().toLowerCase(Locale.ROOT);

			request.setMethod(options.has("method", options) ? switch(method) {
				case "get" -> HttpClient.Method.GET;
				case "post" -> HttpClient.Method.POST;
				case "put" -> HttpClient.Method.PUT;
				case "delete" -> HttpClient.Method.DELETE;
				case "patch" -> HttpClient.Method.PATCH;
				default -> throw new IllegalArgumentException("Unsupported method: " + method);
			} : null);
		}

		request.callAsync(context, new HttpClient.HttpCallback() {
			@Override
			public void onResponse(HttpClient.HttpResponse response) {
				manager.postRunnable(() -> promise.resolve(Context.javaToJS(response, scriptScope)));
			}

			@Override
			public void onError(HttpClient.HttpException exception) {
				manager.postRunnable(() -> promise.reject(Context.javaToJS(exception, scriptScope)));
			}
		});

		return Context.javaToJS(promise, scriptScope);
	}

	public Object getSaved(@NonNull Object key) {
		return prefs.getString(key.toString());
	}

	public void setSaved(@NonNull Object key, @NonNull Object value) {
		prefs.setString(key.toString(), value.toString());
		prefs.saveSync();
	}
}