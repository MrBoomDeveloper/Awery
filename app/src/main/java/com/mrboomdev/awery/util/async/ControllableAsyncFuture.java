package com.mrboomdev.awery.util.async;

@Deprecated(forRemoval = true)
public interface ControllableAsyncFuture<T> extends AsyncFuture<T> {

	void complete(T result) throws Throwable;

	void fail(Throwable throwable);

	interface Callback<T> {
		void start(ControllableAsyncFuture<T> future) throws Throwable;
	}
}