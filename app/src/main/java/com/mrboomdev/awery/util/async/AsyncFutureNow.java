package com.mrboomdev.awery.util.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AsyncFutureNow<T> implements AsyncFuture<T> {

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
		return get();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}
}