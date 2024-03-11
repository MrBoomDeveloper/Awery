package com.mrboomdev.awery.catalog.anilist;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.anilist.query.AnilistQuery;
import com.mrboomdev.awery.util.io.HttpClient;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.util.HashMap;
import java.util.Map;

import com.mrboomdev.awery.R;
import ani.awery.connections.anilist.Anilist;

public class AnilistApi {
	public static final String DOMAIN = "https://graphql.anilist.co";

	public static void __executeQueryImpl(@NonNull AnilistQuery<?> query, AnilistQuery.ResponseCallback<String> callback, AnilistQuery.ResponseCallback<Throwable> exceptionCallback) throws HttpClient.HttpException {
		var data = new HashMap<String, String>() {{
			put("query", query.getQuery());
			put("variables", query.getVariables());
		}};

		var headers = new HashMap<String, String>() {{
			put("Content-Type", "application/json");
			put("Accept", "application/json");
		}};

		if(query.useToken() && Anilist.INSTANCE.getToken() != null) {
			headers.put("Authorization", "Bearer " + Anilist.INSTANCE.getToken());
		}

		var moshi = new Moshi.Builder().build();
		var adapter = moshi.adapter(Types.newParameterizedType(Map.class, String.class, String.class));
		var json = adapter.toJson(data);

		HttpClient.postJson(DOMAIN, json, headers, new HttpClient.HttpCallback() {

			@Override
			public void onResponse(HttpClient.HttpResponse response) {
				if(!response.getText().startsWith("{")) {
					var context = AweryApp.getAnyContext();
					throw new RuntimeException(context.getString(R.string.anilist_down));
				}

				callback.onResponse(response.getText());
			}

			@Override
			public void onError(HttpClient.HttpException e) {
				exceptionCallback.onResponse(e);
			}
		});
	}
}