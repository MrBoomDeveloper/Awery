package com.mrboomdev.awery.util.async;

public interface ControllableAsyncFuture<T> extends AsyncFuture<T> {

	void complete(T result);

	void fail(Throwable throwable);
}