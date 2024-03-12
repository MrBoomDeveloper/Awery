package com.mrboomdev.awery.catalog.anilist.query;

import com.mrboomdev.awery.catalog.anilist.AnilistApi;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.graphql.GraphQLAdapter;
import com.mrboomdev.awery.util.graphql.GraphQLParser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AnilistQuery<T> {
	private ResponseCallback<Throwable> exceptionCallback;
	private Runnable finallyCallback;
	private Throwable e;
	private boolean didFinished;

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
			} catch(Exception e) {
				resolveException(new RuntimeException("Failed to process a json! " + response, e));
				return;
			}

			if(processed instanceof Collection<?> collection) {
				if(collection.isEmpty()) {
					resolveException(new ZeroResultsException("Zero results were found!"));
					return;
				}
			}

			callback.onResponse(processed);
			didFinished = true;

			if(finallyCallback != null) {
				finallyCallback.run();
			}
		}, this::resolveException);

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

	public AnilistQuery<T> onFinally(Runnable callback) {
		if(didFinished) {
			callback.run();
		}

		this.finallyCallback = callback;
		return this;
	}

	private synchronized void resolveException(Throwable e) {
		if(e == null) return;
		this.didFinished = true;
		this.e = e;

		if(exceptionCallback != null) {
			exceptionCallback.onResponse(e);
		}

		if(finallyCallback != null) {
			finallyCallback.run();
		}
	}

	public interface ResponseCallback<T> {
		void onResponse(T response);
	}

	public enum MediaSort {
		SCORE,
		SCORE_DESC,
		POPULARITY,
		POPULARITY_DESC,
		TRENDING,
		TRENDING_DESC,
		UPDATED_AT,
		UPDATED_AT_DESC,
		SEARCH_MATCH,
		FAVOURITES,
		FAVOURITES_DESC
	}
}