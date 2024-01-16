package com.mrboomdev.awery.anilist.query;

import com.mrboomdev.awery.anilist.AnilistApi;
import com.mrboomdev.awery.anilist.data.AnilistDataWrapper;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

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
		AnilistApi.executeQuery(this, response -> {
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