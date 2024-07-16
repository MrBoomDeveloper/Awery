package com.mrboomdev.awery.util.async;

public interface ControllableAsyncFutureCallback<T> {
	void start(ControllableAsyncFuture<T> future);
}