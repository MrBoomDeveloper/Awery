package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.util.concurrent.Flow;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import rx.Observable;
import rx.Subscriber;

public class CoroutineUtil {

	@NonNull
	@Contract("_ -> new")
	public static <T> Continuation<T> createSimpleContinuation(CoroutineCallback<T> callback) {
		return new Continuation<>() {
			@NonNull
			@Override
			public CoroutineContext getContext() {
				return EmptyCoroutineContext.INSTANCE;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void resumeWith(@NonNull Object o) {
				if(o instanceof Result.Failure fail) {
					callback.onResult(null, fail.exception);
					return;
				}

				callback.onResult((T) o, null);
			}
		};
	}

	public static <T> void getObservableValue(@NonNull Observable<T> observable, CoroutineCallback<T> callback) {
		observable.single().subscribe(new Subscriber<>() {
			@Override
			public void onCompleted() {}

			@Override
			public void onStart() {
				request(1);
			}

			@Override
			public void onError(Throwable e) {
				callback.onResult(null, e);
			}

			@Override
			public void onNext(T t) {
				callback.onResult(t, null);
			}
		});
	}

	public interface CoroutineCallback<T> {
		void onResult(@Nullable T result, @Nullable Throwable exception);
	}
}