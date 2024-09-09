package com.mrboomdev.awery.util.async;

public abstract class ControllableAsyncFuture<T> extends AsyncFuture<T> {

	public abstract void complete(T result) throws Throwable;

	public abstract void fail(Throwable throwable);

	public interface Callback<T> {
		void start(ControllableAsyncFuture<T> future) throws Throwable;
	}
}