package com.mrboomdev.awery.util;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpClient {
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final OkHttpClient client = new OkHttpClient();
	private static int usedIds = 0;

	public static OkHttpClient getClient() {
		return client;
	}

	public static void run(Method method, String url, String body, MediaType contentType, Map<String, String> headers, HttpCallback callback) {
		if(usedIds > 1000) usedIds = 0;
		usedIds++;

		var thread = new Thread(() -> {
			var request = new Request.Builder().url(url);

			if(headers != null) {
				for(var entry : headers.entrySet()) {
					request = request.addHeader(entry.getKey(), entry.getValue());
				}
			}

			request = switch(method) {
				case GET -> request.get();
				case POST -> request.post(RequestBody.create(body, contentType));
			};

			var call = client.newCall(request.build());

			try(var response = call.execute()) {
				var _response = new HttpResponse();
				_response.text = response.body().string();

				callback.onResponse(_response);
			} catch(IOException e) {
				callback.onError(new HttpException(e));
			}
		}, "HttpClientThread" + usedIds);

		thread.setUncaughtExceptionHandler((t, e) ->
				callback.onError(new HttpException(e)));

		thread.start();
	}

	public static void get(String url, HttpCallback callback) {
		run(Method.GET, url, null, null, null, callback);
	}

	public static void get(String url, Map<String, String> headers, HttpCallback callback) {
		run(Method.GET, url, null, null, headers, callback);
	}

	public static void post(String url, String body, MediaType contentType, HttpCallback callback) {
		run(Method.POST, url, body, contentType, null, callback);
	}

	public static void post(String url, String body, MediaType contentType, Map<String, String> headers, HttpCallback callback) {
		run(Method.POST, url, body, contentType, headers, callback);
	}

	public static void postJson(String url, String body, HttpCallback callback) {
		run(Method.POST, url, body, JSON, null, callback);
	}

	/**
	 * @see #postJson(String, String, HttpCallback)
	 */
	public static void postJson(String url, String body, Map<String, String> headers, HttpCallback callback) {
		run(Method.POST, url, body, JSON, headers, callback);
	}

	public enum Method {
		GET, POST
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

		public HttpException(String text, Throwable t) {
			super(text, t);
		}
	}
}