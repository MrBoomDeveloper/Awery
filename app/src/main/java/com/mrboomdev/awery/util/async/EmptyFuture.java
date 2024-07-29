package com.mrboomdev.awery.util.async;

import java.util.concurrent.Callable;

public interface EmptyFuture extends BaseFuture {
	void addCallback(Callback callback);

	default EmptyFuture thenEmpty(Callable1 runnable) {
		return AsyncUtils.controllableEmptyFuture(future -> addCallback(new Callback() {
			@Override
			public void onSuccess() {
				AsyncUtils.thread(() -> {
					try {
						runnable.run();
						future.complete();
					} catch(Throwable e) {
						future.fail(e);
					}
				});
			}

			@Override
			public void onFailure(Throwable t) {
				future.fail(t);
			}
		}));
	}

	default <E> AsyncFuture<E> then(Callable<E> runnable) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback() {
			@Override
			public void onSuccess() {
				AsyncUtils.thread(() -> {
					try {
						future.complete(runnable.call());
					} catch(Throwable e) {
						future.fail(e);
					}
				});
			}

			@Override
			public void onFailure(Throwable t) {
				future.fail(t);
			}
		}));
	}

	default <E> AsyncFuture<E> thenControllable(ControllableAsyncFuture.Callback<E> callback) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback() {
			@Override
			public void onSuccess() {
				AsyncUtils.controllableFuture(callback).addCallback(new AsyncFuture.Callback<>() {
					@Override
					public void onSuccess(E result) throws Throwable {
						future.complete(result);
					}

					@Override
					public void onFailure(Throwable t) {
						future.fail(t);
					}
				});
			}

			@Override
			public void onFailure(Throwable t) {
				future.fail(t);
			}
		}));
	}

	default EmptyFuture thenEmptyControllable(ControllableEmptyFuture.Callback callback) {
		return AsyncUtils.controllableEmptyFuture(future -> addCallback(new Callback() {
			@Override
			public void onSuccess() {
				AsyncUtils.controllableEmptyFuture(callback).addCallback(new Callback() {
					@Override
					public void onSuccess() throws Throwable {
						future.complete();
					}

					@Override
					public void onFailure(Throwable t) {
						future.fail(t);
					}
				});
			}

			@Override
			public void onFailure(Throwable t) {
				future.fail(t);
			}
		}));
	}

	default void await() {
		AsyncUtils.await(this::isDone);

		var t = getThrowable();

		if(t != null) {
			throw t instanceof RuntimeException runtimeException
					? runtimeException : new RuntimeException(t);
		}
	}

	interface Callable1 {
		void run() throws Throwable;
	}

	interface Callback {
		void onSuccess() throws Throwable;

		void onFailure(Throwable t);
	}
}