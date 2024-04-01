package com.mrboomdev.awery.extensions.support.anilist.query;

import android.content.Context;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.util.MimeTypes;
import com.mrboomdev.awery.util.exceptions.HttpException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.graphql.GraphQLAdapter;
import com.mrboomdev.awery.util.graphql.GraphQLParser;
import com.mrboomdev.awery.util.io.HttpClient;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
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
		return 60_000;
	}

	private void executeQueryHttp(Context context, ResponseCallback<String> callback) throws HttpClient.HttpException {
		var data = new HashMap<String, String>() {{
			put("query", getQuery());
			put("variables", getVariables());
		}};

		if(useToken()/* && Anilist.INSTANCE.getToken() != null*/) {
			//headers.put("Authorization", "Bearer " + Anilist.INSTANCE.getToken());
		}

		var moshi = new Moshi.Builder().build();
		var adapter = moshi.adapter(Types.newParameterizedType(Map.class, String.class, String.class));
		var json = adapter.toJson(data);

		HttpClient.post("https://graphql.anilist.co")
				.setBody(json, MimeTypes.JSON)
				.setCache(getCacheTime(), HttpClient.CacheMode.CACHE_FIRST)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.callAsync(context, new HttpClient.HttpCallback() {
					@Override
					public void onResponse(HttpClient.HttpResponse response) {
						if(!response.getText().startsWith("{")) {
							var context = AweryApp.getAnyContext();
							throw new HttpException(context.getString(R.string.server_down));
						}

						callback.onResponse(response.getText());
					}

					@Override
					public void onError(HttpClient.HttpException e) {
						resolveException(e);
					}
				});
	}

	public AnilistQuery<T> executeQuery(Context context, ResponseCallback<T> callback) {
		executeQueryHttp(context, response -> {
			T processed;

			try {
				processed = processJson(response);
			} catch(Exception e) {
				resolveException(new JsonDataException("Failed to process a json! " + response, e));
				return;
			}

			if(processed instanceof Collection<?> collection) {
				if(collection.isEmpty()) {
					resolveException(new ZeroResultsException("Zero results were found!"));
					return;
				}
			}

			try {
				callback.onResponse(processed);
			} catch(Throwable t) {
				resolveException(t);
			}

			didFinished = true;

			if(finallyCallback != null) {
				finallyCallback.run();
			}
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