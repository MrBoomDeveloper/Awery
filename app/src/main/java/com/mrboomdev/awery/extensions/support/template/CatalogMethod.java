package com.mrboomdev.awery.extensions.support.template;

import com.squareup.moshi.JsonDataException;

import java.io.IOException;

public abstract class CatalogMethod<T> {
	private ResponseCallback<Exception> exceptionCallback;
	private Exception e;

	protected abstract T processJson(String json) throws IOException;

	protected abstract CatalogApi getApi();

	public abstract String getQuery();

	public int getCacheTime() {
		return 25;
	}

	protected CatalogMethod<T> executeMethod(ResponseCallback<T> callback) {
		getApi().executeMethodImplementation(this, response -> {
			T processed;

			try {
				processed = processJson(response);
			} catch(IOException | JsonDataException e) {
				resolveException(new RuntimeException("Failed to process a json! " + response, e));
				return;
			}

			callback.onResponse(processed);
		});

		return this;
	}

	public CatalogMethod<T> catchExceptions(ResponseCallback<Exception> callback) {
		this.exceptionCallback = callback;
		resolveException(e);
		return this;
	}

	private synchronized void resolveException(Exception e) {
		if(e == null) return;
		this.e = e;

		if(exceptionCallback != null) {
			exceptionCallback.onResponse(e);
		}
	}

	public interface ResponseCallback<T> {
		void onResponse(T response);
	}
}