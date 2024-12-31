package com.mrboomdev.awery.util.async;

@Deprecated(forRemoval = true)
public interface ControllableEmptyFuture extends EmptyFuture {
	void complete() throws Throwable;

	void fail(Throwable throwable);

	interface Callback {
		void start(ControllableEmptyFuture future) throws Throwable;
	}
}