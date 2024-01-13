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

	public ParameterizedType getItemWrapperType(Class<?> itemType) {
		return Types.newParameterizedType(AnilistDataWrapper.class, itemType);
	}

	public ParameterizedType getListWrapperType(Class<?> itemType) {
		var type = Types.newParameterizedType(List.class, itemType);
		return Types.newParameterizedType(AnilistDataWrapper.class, type);
	}

	public <E> E simpleItemParse(Class<?> itemClass, String json, String root) throws IOException {
		var type = getItemWrapperType(itemClass);
		return simpleParse(type, json, root);
	}

	public <E> E simpleItemParse(Class<?> itemClass, String json) throws IOException {
		return simpleItemParse(itemClass, json, null);
	}

	public <E> E simpleListParse(Class<?> itemClass, String json, String root) throws IOException {
		var type = getListWrapperType(itemClass);
		return simpleParse(type, json, root);
	}

	public <E> E simpleListParse(Class<?> itemClass, String json) throws IOException {
		return simpleListParse(itemClass, json, null);
	}

	@SuppressWarnings("unchecked")
	public <E> E simpleParse(ParameterizedType type, String json, String root) throws IOException {
		var moshi = new Moshi.Builder().build();
		var adapter = moshi.adapter(type);

		var data = adapter.fromJson(json);
		if(data == null) return null;

		if(data instanceof AnilistDataWrapper<?> wrapper) {
			return (E)(root != null ? wrapper.get(root) : wrapper.first());
		}

		throw new RuntimeException("Not instanceof AnilistDataWrapper!");
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