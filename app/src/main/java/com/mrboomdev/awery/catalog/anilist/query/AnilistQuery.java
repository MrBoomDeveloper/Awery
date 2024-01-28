package com.mrboomdev.awery.catalog.anilist.query;

import com.mrboomdev.awery.catalog.anilist.AnilistApi;
import com.squareup.moshi.JsonDataException;

import java.io.IOException;

public abstract class AnilistQuery<T> {
	private ResponseCallback<Exception> exceptionCallback;
	private Exception e;

	protected abstract T processJson(String json) throws IOException;

	public abstract String getQuery();

	public String getVariables() {
		return "";
	}

	public boolean useToken() {
		return true;
	}

	public int getCacheTime() {
		return 25;
	}

	public AnilistQuery<T> executeQuery(ResponseCallback<T> callback) {
		AnilistApi.__executeQueryImpl(this, response -> {
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

	public AnilistQuery<T> catchExceptions(ResponseCallback<Exception> callback) {
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