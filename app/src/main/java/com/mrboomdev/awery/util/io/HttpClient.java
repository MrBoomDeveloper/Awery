package com.mrboomdev.awery.util.io;

import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static java.util.Objects.requireNonNull;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.data.Constants;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClient {
	private static OkHttpClient client;

	public static OkHttpClient getClient() {
		if(client != null) return client;
		var builder = new OkHttpClient.Builder();

		var cacheDir = new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_NET_CACHE);
		var cache = new Cache(cacheDir, /* 10mb */ 10 * 1024 * 1024);
		builder.cache(cache);

		if(AwerySettings.LOG_NETWORK.getValue()) {
			var httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addNetworkInterceptor(httpLoggingInterceptor);
		}

		client = builder.build();
		return client;
	}

	@NotNull
	public static File downloadSync(@NotNull HttpRequest request, @NotNull File targetFile) throws IOException {
		request.checkFields();
		var url = new URL(request.getUrl());

		requireNonNull(targetFile.getParentFile()).mkdirs();
		targetFile.delete();
		targetFile.createNewFile();

		var connection = url.openConnection();

		if(request.getHeaders() != null) {
			for(var header : request.getHeaders().entrySet()) {
				connection.setRequestProperty(header.getKey(), header.getValue());
			}
		}

		var httpChannel = Channels.newChannel(connection.getInputStream());

		try(var fos = new FileOutputStream(targetFile)) {
			fos.getChannel().transferFrom(httpChannel, 0, Long.MAX_VALUE);
		}

		return targetFile;
	}

	@NonNull
	public static AsyncFuture<File> download(@NonNull HttpRequest request, @NonNull File targetFile) {
		URL url;

		try {
			request.checkFields();
			url = new URL(request.getUrl());
		} catch(Throwable t) {
			return AsyncUtils.futureFailNow(t);
		}

		return AsyncUtils.controllableFuture(future -> {
			try {
				requireNonNull(targetFile.getParentFile()).mkdirs();
				targetFile.delete();
				targetFile.createNewFile();

				var connection = url.openConnection();

				if(request.getHeaders() != null) {
					for(var header : request.getHeaders().entrySet()) {
						connection.setRequestProperty(header.getKey(), header.getValue());
					}
				}

				var httpChannel = Channels.newChannel(connection.getInputStream());

				try(var fos = new FileOutputStream(targetFile)) {
					fos.getChannel().transferFrom(httpChannel, 0, Long.MAX_VALUE);
				}

				future.complete(targetFile);
			} catch(IOException e) {
				future.fail(e);
			}
		});
	}

	@NonNull
	public static HttpResponse fetchSync(@NonNull HttpRequest request) throws IOException {
		request.checkFields();

		var okRequest = new okhttp3.Request.Builder();
		okRequest.url(request.getUrl());

		if(request.getHeaders() != null) {
			for(var entry : request.getHeaders().entrySet()) {
				okRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		switch(request.getMethod()) {
			case GET -> okRequest.get();
			case HEAD -> okRequest.head();
			case DELETE -> okRequest.delete();

			default -> okRequest.method(request.getMethod().name(), request.getForm() != null ? request.getForm().build()
					: RequestBody.create(request.getBody(), request.getMediaType()));
		}

		if(request.getCacheMode() != null && request.getCacheMode().doCache()) {
			okRequest.cacheControl(new CacheControl.Builder()
					.onlyIfCached()
					.maxAge(request.getCacheDuration(), TimeUnit.MILLISECONDS)
					.build());
		}

		return executeCall(okRequest, request.getCacheMode());
	}

	@NonNull
	public static AsyncFuture<HttpResponse> fetch(@NonNull HttpRequest request) {
		return thread(() -> fetchSync(request));
	}

	@NonNull
	private static HttpResponse executeCall(Request.Builder okRequest, HttpCacheMode mode) throws IOException {
		try(var response = getClient().newCall(okRequest.build()).execute()) {
			if(mode != null && mode.doCache() && response.code() == 504) {
				var cacheControl = new CacheControl.Builder().noCache().build();
				return executeCall(okRequest.cacheControl(cacheControl), HttpCacheMode.NETWORK_ONLY);
			}

			return new HttpResponse(response);
		}
	}
}