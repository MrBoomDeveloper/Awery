package com.mrboomdev.awery.util.async;

public abstract class ControllableEmptyFuture extends EmptyFuture {
	public abstract void complete() throws Throwable;

	public abstract void fail(Throwable throwable);

	public interface Callback {
		void start(ControllableEmptyFuture future) throws Throwable;
	}
}