package com.mrboomdev.awery.util.async;

@Deprecated(forRemoval = true)
public interface BaseFuture {
	boolean cancel(boolean mayInterruptIfRunning);

	boolean isCancelled();

	boolean isDone();

	Throwable getThrowable();
}