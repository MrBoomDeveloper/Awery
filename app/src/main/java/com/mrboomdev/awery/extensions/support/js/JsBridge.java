package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpMethod;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.io.HttpResponse;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class JsBridge {
	public final String FILTER_VIDEO_CATEGORY = ExtensionProvider.FILTER_VIDEO_CATEGORY;
	public final String FILTER_SEASON = ExtensionProvider.FILTER_SEASON;
	public final String FILTER_EPISODE = ExtensionProvider.FILTER_EPISODE;
	public final String FILTER_FEED = ExtensionProvider.FILTER_FEED;
	public final String FILTER_PAGE = ExtensionProvider.FILTER_PAGE;
	public final String FILTER_QUERY = ExtensionProvider.FILTER_QUERY;
	public final String FILTER_TAGS = ExtensionProvider.FILTER_TAGS;
	public final String FILTER_MEDIA = ExtensionProvider.FILTER_MEDIA;

	public final String VIDEO_CATEGORY_EPISODE = ExtensionProvider.VIDEO_CATEGORY_EPISODE;
	public final String VIDEO_CATEGORY_TRAILER = ExtensionProvider.VIDEO_CATEGORY_TRAILER;
	public final String VIDEO_CATEGORY_MUSIC = ExtensionProvider.VIDEO_CATEGORY_MUSIC;

	private static final String TAG = "JsBridge";
	protected WeakReference<android.content.Context> context;
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

	private static double parseDouble(Object object) {
		if(object instanceof Number number) {
			return number.doubleValue();
		}

		return Double.parseDouble(object.toString());
	}

	public int compareNumbers(Object a, Object b) {
		return Double.compare(parseDouble(a), parseDouble(b));
	}

	public Object plus(Object a, Object b) {
		return parseDouble(a) + parseDouble(b);
	}

	public Object currentTime() {
		return System.currentTimeMillis();
	}

	public String getAdultMode() {
		var adultMode = AwerySettings.ADULT_MODE.getValue();
		return requireNonNullElse(adultMode, AwerySettings.AdultMode_Values.SAFE).name();
	}

	@NonNull
	public static NativeObject createObject(@NonNull Context context, Scriptable scope, @NonNull Map<?, ?> map) {
		var obj = (NativeObject) context.newObject(scope);
		obj.putAll(map);
		return obj;
	}

	public Object fetch(@NonNull ScriptableObject options) {
		var promise = new JsPromise(scriptScope);

		var request = new HttpRequest();
		request.setUrl(stringFromJs(options.get("url")));

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

		if(!isNullJs(options.get("body", options))) {
			var body = requireArgument(options, "body", String.class);
			var contentTypeSpecified = stringFromJs(options, "contentType");

			var contentType = contentTypeSpecified == null ? null
					: StringUtils.parseEnum(contentTypeSpecified, MimeTypes.class);

			if(contentType == null) request.setBody(body, contentTypeSpecified);
			else request.setBody(body, contentType);

			if(isNullJs(options.get("method", options))) {
				request.setMethod(HttpMethod.POST);
			}
		}

		if(!isNullJs(options.get("form", options))) {
			var obj = (NativeObject) options.get("form");

			for(var entry : obj.entrySet()) {
				var value = entry.getValue();

				if(value == null) {
					continue;
				}

				request.addFormField(entry.getKey().toString(), value.toString());
			}

			if(isNullJs(options.get("method", options))) {
				request.setMethod(HttpMethod.POST);
			}
		}

		doIfNotNull(stringFromJs(options.get("method")), method -> {
			request.setMethod(HttpMethod.valueOf(method));

			if(isNullJs(options.get("form")) && isNullJs(options.get("body", options))) {
				switch(request.getMethod()) {
					case PUT, PATCH, POST -> request.setBody("", MimeTypes.TEXT);
				}
			}
		});

		HttpClient.fetch(request).addCallback(new AsyncFuture.Callback<>() {

			@Override
			public void onSuccess(HttpResponse response) {
				manager.postRunnable(() -> promise.resolve(Context.javaToJS(response, scriptScope)));
			}

			@Override
			public void onFailure(Throwable t) {
				manager.postRunnable(() -> promise.reject(Context.javaToJS(t, scriptScope)));
			}
		});

		return Context.javaToJS(promise, scriptScope);
	}

	@Nullable
	public static <A, B> A returnIfNotNullJs(B object, Callbacks.Result1<A, B> function) {
		return isNullJs(object) ? null : function.run(object);
	}

	public static int intFromJs(Object object) {
		var result = fromJs(object, Integer.class);
		return result == null ? 0 : result;
	}

	public static boolean booleanFromJs(Object object) {
		var result = fromJs(object, Boolean.class);
		return result != null ? result : false;
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
		if(object instanceof NativeJavaObject o) {
			object = o.unwrap();
		}

		if(isNullJs(object)) return null;
		return object.toString();
	}

	public static String stringFromJs(@NonNull ScriptableObject scope, String name) {
		return stringFromJs(scope.get(name, scope));
	}

	@Nullable
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> List<T> listFromJs(Object object, Class<T> clazz) {
		if(object instanceof NativeJavaObject o) {
			object = o.unwrap();
		}

		if(isNullJs(object)) return null;

		return stream((NativeArray) object)
				.map(item -> fromJs(item, clazz))
				.toList();
	}

	@Nullable
	public static <T> T fromJs(Object object, Class<T> clazz) {
		if(object instanceof NativeJavaObject o) {
			object = o.unwrap();
		}

		if(isNullJs(object)) return null;

		if(clazz == Integer.class) return clazz.cast(((Number) object).intValue());
		if(clazz == Float.class) return clazz.cast(((Number) object).floatValue());
		if(clazz == Long.class) return clazz.cast(((Number) object).longValue());
		if(clazz == String.class) return clazz.cast(object.toString());

		return clazz.cast(object);
	}

	public static boolean isNullJs(Object o) {
		return o == null || Undefined.isUndefined(o) || o == UniqueTag.NOT_FOUND || o == UniqueTag.NULL_VALUE;
	}

	public static <T> T notNullJs(T t) {
		return isNullJs(t) ? null : t;
	}

	public void log(Object o) {
		Log.i(TAG, "\"" + provider.getName() + "\" logged: " + o);
	}

	public Object getSaved(@NonNull Object key) {
		return getSettings().getString(key.toString());
	}

	public void setSaved(@NonNull Object key, Object value) {
		var settings = getSettings();

		settings.setValue(key.toString(), value != null ? value.toString() : null);
		settings.saveSync();
	}

	@NonNull
	private NicePreferences getSettings() {
		var context = this.context.get();

		if(context == null) {
			throw new NullPointerException("Context was been cleared by a garbage collector!");
		}

		return getPrefs("JsBridge-" + provider.id);
	}
}