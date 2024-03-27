package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryApp.getAnyContext;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.util.Callbacks;
import com.mrboomdev.awery.util.MimeTypes;
import com.mrboomdev.awery.util.io.HttpClient;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class JsBridge {
	private final AwerySettings prefs;
	private final JsManager manager;

	public JsBridge(JsManager manager, Context context, String id) {
		this.prefs = AwerySettings.getInstance(context, "JsBridge-" + id);
		this.manager = manager;
	}

	@JSFunction
	public void toast(Object object) {
		AweryApp.toast(object);
	}

	@JSFunction
	public Promise<Response, RuntimeException> fetch(@NonNull ScriptableObject options) {
		var context = getAnyContext();
		var promise = new Promise<Response, RuntimeException>();

		var jsHeaders = options.get("headers");
		Map<String, String> headers = null;

		if(jsHeaders != null) {
			headers = new HashMap<>();
			//options.get
		}

		new HttpClient.Request()
				.setUrl((String) options.get("url"))
				.setBody((String) options.get("body"), MimeTypes.ANY)
				.setMethod(options.has("method", options) ? switch(((String) options.get("method")).toLowerCase(Locale.ROOT)) {
					case "get" -> HttpClient.Method.GET;
					case "post" -> HttpClient.Method.POST;
					case "put" -> HttpClient.Method.PUT;
					case "delete" -> HttpClient.Method.DELETE;
					case "patch" -> HttpClient.Method.PATCH;
					default -> throw new IllegalArgumentException("Unsupported method: " + options.get("method"));
				} : null)
				.callAsync(context, new HttpClient.HttpCallback() {
			@Override
			public void onResponse(HttpClient.HttpResponse response) {
				manager.postRunnable(() -> {
					var res = new Response();
					res.body = response.getText();
					res.statusCode = response.getStatusCode();
					promise.resolve(res);
				});
			}

			@Override
			public void onError(HttpClient.HttpException exception) {
				manager.postRunnable(() -> promise.reject(exception));
			}
		});

		return promise;
	}

	@JSFunction
	public String getSavedValue(String key) {
		return prefs.getString(key);
	}

	@JSFunction
	public void saveValue(String key, String value) {
		prefs.setString(key, value);
		prefs.saveSync();
	}

	public static class Response {
		public String body;
		public int statusCode;
	}

	public static class Promise<T, E> {
		private Callbacks.Callback1<T> resolve;
		private Callbacks.Callback1<E> reject;
		private T result;
		private E error;

		@JSFunction
		public void resolve(T object) {
			this.result = object;

			if(resolve != null) {
				resolve.run(object);
			}
		}

		@JSFunction
		public void reject(E object) {
			this.error = object;

			if(reject != null) {
				reject.run(object);
			}
		}

		@JSFunction(value = "then")
		public Promise<T, E> _then(Callbacks.Callback1<T> callback) {
			this.resolve = callback;

			if(result != null) {
				this.resolve(result);
			}

			return this;
		}

		@JSFunction(value = "catch")
		public Promise<T, E> _catch(Callbacks.Callback1<E> callback) {
			this.reject = callback;

			if(error != null) {
				this.reject(error);
			}

			return this;
		}
	}
}