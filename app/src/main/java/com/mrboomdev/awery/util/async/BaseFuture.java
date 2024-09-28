package com.mrboomdev.awery.util.async;

public interface BaseFuture {
	boolean cancel(boolean mayInterruptIfRunning);

	boolean isCancelled();

	boolean isDone();

	Throwable getThrowable();
}