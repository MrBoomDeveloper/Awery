package com.mrboomdev.awery.catalog.anilist.query;

import com.mrboomdev.awery.catalog.anilist.AnilistApi;
import com.mrboomdev.awery.catalog.anilist.data.AnilistTrendingMedia;
import com.mrboomdev.awery.util.graphql.GraphQLAdapter;
import com.mrboomdev.awery.util.graphql.GraphQLParser;
import com.squareup.moshi.JsonDataException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class AnilistQuery<T> {
	private ResponseCallback<Throwable> exceptionCallback;
	private Throwable e;

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

	@SuppressWarnings("unchecked")
	protected <E> List<E> parsePageList(Type childType, String json) throws IOException {
		var listType = GraphQLParser.getTypeWithGenerics(List.class, childType);
		var pageType = GraphQLParser.getTypeWithGenerics(Map.class, String.class, listType);
		var wrapper = new GraphQLAdapter<Map<String, List<E>>>(pageType).parseFirst(json);
		var data = wrapper.values().toArray()[0];

		return (List<E>) data;
	}

	public AnilistQuery<T> catchExceptions(ResponseCallback<Throwable> callback) {
		this.exceptionCallback = callback;
		resolveException(e);
		return this;
	}

	private synchronized void resolveException(Throwable e) {
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