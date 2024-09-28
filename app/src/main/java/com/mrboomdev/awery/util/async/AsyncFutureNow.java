package com.mrboomdev.awery.util.async;

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
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}
}