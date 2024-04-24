package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.util.io.HttpClient;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

import java.util.HashMap;
import java.util.Locale;

/**
 * Do not ever init this class by yourself!
 * @author MrBoomDev
 */
@SuppressWarnings("unused")
public class JsBridge {
	private static final String TAG = "JsBridge";
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
	public void setManifest(NativeObject object) {
		provider.finishInit(this, object);
	}

	public void toast(Object object) {
		AweryApp.toast(object);
	}

	public Object fetch(@NonNull ScriptableObject options) {
		var promise = new JsPromise(scriptScope);

		var request = new HttpClient.Request()
				.setUrl(options.get("url").toString());

		if(options.get("headers") instanceof NativeObject o) {
			var headers = new HashMap<String, String>();

			for(var entry : o.entrySet()) {
				var value = entry.getValue();

				if(value == null) {
					continue;
				}

				headers.put(entry.getKey().toString(), value.toString());
			}

			request.setHeaders(headers);
		}

		if(options.has("body", options)) {
			var contentTypeSpecified = options.has("contentType", options)
					? options.get("contentType").toString() : null;

			var contentType = contentTypeSpecified == null ? null
					: StringUtils.parseEnum(contentTypeSpecified.toUpperCase(Locale.ROOT), MimeTypes.class);

			if(contentType == null) request.setBody(options.get("body").toString(), contentTypeSpecified);
			else request.setBody(options.get("body").toString(), contentType);

			if(!options.has("method", options)) {
				request.setMethod(HttpClient.Method.POST);
			}
		}

		if(options.has("form", options)) {
			var obj = (NativeObject) options.get("form");

			for(var entry : obj.entrySet()) {
				var value = entry.getValue();

				if(value == null) {
					continue;
				}

				request.addFormField(entry.getKey().toString(), value.toString());
			}

			if(!options.has("method", options)) {
				request.setMethod(HttpClient.Method.POST);
			}
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

		request.callAsync(getAnyContext(), new HttpClient.HttpCallback() {
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

	public static int intFromJs(Object object) {
		var result = fromJs(object, Integer.class);
		return result == null ? 0 : result;
	}

	public static boolean booleanFromJs(Object object) {
		var result = fromJs(object, Boolean.class);
		return result != null && result;
	}

	public static float floatFromJs(Object object) {
		var result = fromJs(object, Float.class);
		return result == null ? 0 : result;
	}

	public static long longFromJs(Object object) {
		var result = fromJs(object, Long.class);
		return result == null ? 0 : result;
	}

	@Nullable
	public static String stringFromJs(Object object) {
		if(isNull(object)) return null;
		return object.toString();
	}

	@Nullable
	public static <T> T fromJs(Object object, Class<T> clazz) {
		if(isNull(object)) return null;

		if(clazz.isAssignableFrom(String.class)) return clazz.cast(object.toString());
		if(clazz == Integer.class) return clazz.cast(((Number) object).intValue());
		if(clazz == Float.class) return clazz.cast(((Number) object).floatValue());
		if(clazz == Long.class) return clazz.cast(((Number) object).longValue());

		return clazz.cast(object);
	}

	public static boolean isNull(Object o) {
		return o == null || Undefined.isUndefined(o) || o == UniqueTag.NOT_FOUND || o == UniqueTag.NULL_VALUE;
	}

	public void log(Object o) {
		Log.i(TAG, "\"" + provider.getName() + "\" logged: " + o);
	}

	public Object getSaved(@NonNull Object key) {
		return prefs.getString(key.toString());
	}

	public void setSaved(@NonNull Object key, Object value) {
		prefs.setString(key.toString(), value != null ? value.toString() : null);
		prefs.saveSync();
	}
}