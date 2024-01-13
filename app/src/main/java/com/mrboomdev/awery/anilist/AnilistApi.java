package com.mrboomdev.awery.anilist;

import androidx.annotation.NonNull;

import com.lagradost.nicehttp.NiceResponse;
import com.mrboomdev.awery.KotlinBridge;
import com.mrboomdev.awery.KotlinToJava;
import com.mrboomdev.awery.anilist.query.AnilistQuery;

import java.util.HashMap;
import java.util.Objects;

import ani.awery.App;
import ani.awery.R;
import ani.awery.connections.anilist.Anilist;
import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class AnilistApi {
	public static final String DOMAIN = "https://graphql.anilist.co";

	public static void executeQuery(@NonNull AnilistQuery<?> query, AnilistQuery.ResponseCallback<String> callback) {
		var data = new HashMap<String, String>() {{
			put("query", query.getQuery());
			put("variables", query.getVariables());
		}};

		var headers = new HashMap<String, String>() {{
			put("Content-Type", "application/json");
			put("Accept", "application/json");
		}};

		if(query.useToken()) {
			headers.put("Authorization", "Bearer " + Anilist.INSTANCE.getToken());
		}

		KotlinBridge.clientPost(DOMAIN, headers, data, 10, response -> {
			if(!response.getText().startsWith("{")) {
				var context = Objects.requireNonNull(App.Companion.getContext());
				throw new RuntimeException(context.getString(R.string.anilist_down));
			}

			callback.onResponse(response.getText());
		});
	}
}