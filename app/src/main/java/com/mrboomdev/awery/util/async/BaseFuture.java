package com.mrboomdev.awery.util.async;

public abstract class BaseFuture {
	public abstract boolean cancel(boolean mayInterruptIfRunning);

	public abstract boolean isCancelled();

	public abstract boolean isDone();

	protected boolean hasResult() {
		return isDone();
	}

	public abstract Throwable getThrowable();
}