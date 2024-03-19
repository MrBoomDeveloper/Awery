package com.mrboomdev.awery.util.io;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.data.settings.AwerySettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClient {
	private static final String TAG = "HttpClient";
	private static final OkHttpClient client;

	static {
		var builder = new OkHttpClient.Builder();

		var cacheDir = new File(
				AweryApp.getContext().getCacheDir(),
				Constants.DIRECTORY_NET_CACHE);

		var cache = new Cache(cacheDir, /* 10mb */ 10 * 1024 * 1024);
		builder.cache(cache);

		if(AwerySettings.getInstance().getBoolean(AwerySettings.VERBOSE_NETWORK)) {
			var httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addNetworkInterceptor(httpLoggingInterceptor);
		}

		client = builder.build();
	}

	public static class Request {
		private ContentType contentType;
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

		public Request addHeader(String key, String value) {
			if(headers == null) headers = new HashMap<>();
			headers.put(key, value);
			return this;
		}

		public Request setBody(String body, ContentType contentType) {
			this.body = body;
			this.contentType = contentType;
			return this;
		}

		public void call(HttpCallback callback) {
			call(callback, true);
		}

		public void callAsync(HttpCallback callback) {
			checkFields();

			var thread = new Thread(() -> call(callback, false));
			thread.setName("HttpClient-" + thread.getId());

			thread.setUncaughtExceptionHandler((t, e) ->
					callback.onError(new HttpException(e)));

			thread.start();
		}

		private void call(HttpCallback callback, boolean check) {
			if(check) checkFields();
			HttpClient.call(this, callback);
		}

		private void checkFields() {
			if(url == null) throw new NullPointerException("Url not set!");
			if(method == null) throw new NullPointerException("Method not set!");

			if(method == Method.POST && body == null) {
				throw new NullPointerException("Method is POST but body was not set!");
			}

			if(cacheMode == CacheMode.CACHE_FIRST && cacheTime == null) {
				throw new NullPointerException("Cache mode is CACHE_FIRST but cache time was not set!");
			}
		}
	}

	public static OkHttpClient getClient() {
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

	private static void call(@NonNull Request request, HttpCallback callback) {
		var okRequest = new okhttp3.Request.Builder();
		okRequest.url(request.url);

		if(request.headers != null) {
			for(var entry : request.headers.entrySet()) {
				okRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		switch(request.method) {
			case GET -> okRequest.get();
			case POST -> okRequest.post(RequestBody.create(request.body, request.contentType.type));
		}

		if(request.cacheTime != null) {
			okRequest.cacheControl(new CacheControl.Builder()
					.onlyIfCached()
					.maxAge(request.cacheTime, TimeUnit.MILLISECONDS)
					.build());
		}

		executeCall(okRequest, request.cacheMode, callback);
	}

	private static void executeCall(okhttp3.Request.Builder okRequest, CacheMode mode, HttpCallback callback) {
		try(var response = client.newCall(okRequest.build()).execute()) {
			if(mode == CacheMode.CACHE_FIRST && response.code() == 504) {
				var cacheControl = new CacheControl.Builder().noCache().build();
				executeCall(okRequest.cacheControl(cacheControl), CacheMode.NETWORK_ONLY, callback);
				return;
			}

			var _response = new HttpResponse();
			_response.text = response.body().string();
			callback.onResponse(_response);
		} catch(IOException e) {
			callback.onError(new HttpException(e));
		}
	}

	public enum Method { GET, POST }
	public enum CacheMode { NETWORK_ONLY, CACHE_FIRST }

	public enum ContentType {
		JSON(MediaType.parse("application/json; charset=utf-8"));

		private final MediaType type;

		ContentType(MediaType type) {
			this.type = type;
		}
	}

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

	public static class HttpResponse {
		protected String text;

		public String getText() {
			return text;
		}
	}

	public static class HttpException extends RuntimeException {

		public HttpException(Throwable t) {
			super(t);
		}
	}
}