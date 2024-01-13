package com.mrboomdev.awery;

import androidx.annotation.NonNull;

import com.lagradost.nicehttp.NiceResponse;
import com.mrboomdev.awery.anilist.query.AnilistQuery;

import java.util.Map;
import java.util.Objects;

import ani.awery.App;
import ani.awery.R;
import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
public class KotlinBridge {

	public static void clientPost(
			String url,
			Map<String, String> headers,
			Map<String, String> data,
			int cacheTime,
			AnilistQuery.ResponseCallback<NiceResponse> responseCallback
	) {
		KotlinToJava.INSTANCE.clientPost(url, headers, data, cacheTime, new Continuation<>() {
			@NonNull
			@Override
			public CoroutineContext getContext() {
				return EmptyCoroutineContext.INSTANCE;
			}

			@Override
			public void resumeWith(@NonNull Object o) {
				if(o instanceof Result.Failure) {
					throw new RuntimeException("Failed to fetch!");
				}

				responseCallback.onResponse((NiceResponse) o);
			}
		});
	}
}