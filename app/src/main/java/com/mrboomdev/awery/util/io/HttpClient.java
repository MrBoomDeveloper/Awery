package com.mrboomdev.awery.util.io;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.util.MimeTypes;

import org.mozilla.javascript.annotations.JSGetter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClient {
	private static final String TAG = "HttpClient";
	private static OkHttpClient client;

	public static class Request {
		private FormBody.Builder form;
		private MediaType mediaType;
		private Map<String, String> headers;
		private CacheMode cacheMode;
		private Method method;
		private String url, body;
		private Integer cacheTime;

		public Request setMethod(Method method) {
			this.method = method;
			return this;
		}

		public Request setUrl(String url) {
			this.url = url;
			return this;
		}

		public Request setCache(Integer duration, CacheMode cacheMode) {
			this.cacheTime = duration;
			this.cacheMode = cacheMode;
			return this;
		}

		public Request disableCache() {
			this.cacheMode = CacheMode.NETWORK_ONLY;
			this.cacheTime = 0;
			return this;
		}

		public Request setForm(Map<String, String> map) {
			if(map == null) {
				this.form = null;
				return this;
			}

			this.form = new FormBody.Builder();

			for(var entry : map.entrySet()) {
				form.add(entry.getKey(), entry.getValue());
			}

			return this;
		}

		public Request addFormField(String key, String value) {
			if(form == null) form = new FormBody.Builder();
			form.add(key, value);
			return this;
		}

		public Request setHeaders(Map<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public Request addHeader(String key, String value) {
			if(headers == null) headers = new HashMap<>();
			headers.put(key, value);
			return this;
		}

		public Request setBody(String body, MimeTypes contentType) {
			if(body == null) return this;

			this.body = body;
			this.mediaType = contentType.toMediaType();
			return this;
		}

		public Request setBody(String body, String contentType) {
			if(body == null) return this;

			this.body = body;
			this.mediaType = MediaType.parse(contentType);
			return this;
		}

		public void call(Context context, HttpCallback callback) {
			call(context, callback, true);
		}

		public void callAsync(Context context, HttpCallback callback) {
			checkFields();

			var thread = new Thread(() -> call(context, callback, false));
			thread.setName("HttpClient-" + thread.getId());

			thread.setUncaughtExceptionHandler((t, e) ->
					callback.onError(new HttpException(e)));

			thread.start();
		}

		private void call(Context context, HttpCallback callback, boolean check) {
			if(check) checkFields();
			HttpClient.call(context, this, callback);
		}

		private void checkFields() {
			if(url == null) throw new NullPointerException("Url not set!");

			if(method == null) {
				method = (body == null) ? Method.GET : Method.POST;
			}

			if(form == null && body == null) {
				if(method == Method.POST) throw new IllegalArgumentException("Body or form must be set for method POST!");
				if(method == Method.PUT) throw new IllegalArgumentException("Body or form must be set for method PUT!");
				if(method == Method.PATCH) throw new IllegalArgumentException("Body or form must be set for method PATCH!");
			}

			if(form != null && body != null) {
				throw new IllegalArgumentException("Body and form cannot be set at the same time!");
			}

			if(cacheMode == CacheMode.CACHE_FIRST && cacheTime == null) {
				throw new NullPointerException("Cache mode is CACHE_FIRST but cache time was not set!");
			}
		}
	}

	public static OkHttpClient getClient(Context context) {
		if(client != null) return client;

		var builder = new OkHttpClient.Builder();

		var cacheDir = new File(context.getCacheDir(), Constants.DIRECTORY_NET_CACHE);
		var cache = new Cache(cacheDir, /* 10mb */ 10 * 1024 * 1024);
		builder.cache(cache);

		if(AwerySettings.getInstance().getBoolean(AwerySettings.VERBOSE_NETWORK)) {
			var httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addNetworkInterceptor(httpLoggingInterceptor);
		}

		client = builder.build();
		return client;
	}

	public static Request get(String url) {
		return new Request()
				.setMethod(Method.GET)
				.setUrl(url);
	}

	public static Request post(String url) {
		return new Request()
				.setMethod(Method.POST)
				.setUrl(url);
	}

	private static void call(Context context, @NonNull Request request, HttpCallback callback) {
		var okRequest = new okhttp3.Request.Builder();
		okRequest.url(request.url);

		if(request.headers != null) {
			for(var entry : request.headers.entrySet()) {
				okRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		switch(request.method) {
			case GET -> okRequest.get();
			case HEAD -> okRequest.head();
			case DELETE -> okRequest.delete();

			case PATCH -> okRequest.patch(request.form != null ? request.form.build()
					: RequestBody.create(request.body, request.mediaType));

			case PUT -> okRequest.put(request.form != null ? request.form.build()
					: RequestBody.create(request.body, request.mediaType));

			case POST -> okRequest.post(request.form != null ? request.form.build()
					: RequestBody.create(request.body, request.mediaType));
		}

		if(request.cacheTime != null) {
			okRequest.cacheControl(new CacheControl.Builder()
					.onlyIfCached()
					.maxAge(request.cacheTime, TimeUnit.MILLISECONDS)
					.build());
		}

		executeCall(context, okRequest, request.cacheMode, callback);
	}

	private static void executeCall(Context context, okhttp3.Request.Builder okRequest, CacheMode mode, HttpCallback callback) {
		try(var response = getClient(context).newCall(okRequest.build()).execute()) {
			if(mode == CacheMode.CACHE_FIRST && response.code() == 504) {
				var cacheControl = new CacheControl.Builder().noCache().build();
				executeCall(context, okRequest.cacheControl(cacheControl), CacheMode.NETWORK_ONLY, callback);
				return;
			}

			callback.onResponse(new HttpResponseImpl(response));
		} catch(IOException e) {
			callback.onError(new HttpException(e));
		}
	}

	public enum Method { GET, POST, DELETE, PUT, PATCH, HEAD }
	public enum CacheMode { NETWORK_ONLY, CACHE_FIRST }

	public interface SimpleHttpCallback extends HttpCallback {
		/**
		 * Called after the request was either successful or not.
		 */
		void onResult(@Nullable HttpResponse response, @Nullable HttpException exception);

		/**
		 * Do not override this method, or else the whole idea of this interface will be lost!
		 */
		@Override
		default void onResponse(HttpResponse response) {
			onResult(response, null);
		}

		/**
		 * Do not override this method, or else the whole idea of this interface will be lost!
		 */
		@Override
		default void onError(HttpException exception) {
			onResult(null, exception);
		}
	}

	public interface HttpCallback {
		void onResponse(HttpResponse response);
		void onError(HttpException exception);
	}

	public interface HttpResponse {
		String getText();
		int getStatusCode();
	}

	private static class HttpResponseImpl implements HttpResponse {
		private final String text;
		private final int code;

		public HttpResponseImpl(@NonNull Response response) throws IOException {
			this.code = response.code();
			this.text = response.body().string();
		}

		@NonNull
		@Override
		@JSGetter("text")
		public String getText() {
			return text;
		}

		@Override
		@JSGetter("statusCode")
		public int getStatusCode() {
			return code;
		}
	}

	public static class HttpException extends RuntimeException {

		public HttpException(Throwable t) {
			super(t);
		}
	}
}