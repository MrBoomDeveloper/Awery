package com.mrboomdev.awery.util.async;

import org.jetbrains.annotations.NotNull;
@Deprecated(forRemoval = true)
public interface AsyncFuture<T> extends BaseFuture {
	void addCallback(Callback<T> callback);

	T getResult();

	default <E> AsyncFuture<E> then(CallableResult<T, E> runnable) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				AsyncUtils.thread(() -> {
					try {
						return runnable.run(result);
					} catch(Throwable e) {
						throw e instanceof RuntimeException runtimeException
								? runtimeException : new RuntimeException(e);
					}
				}).addCallback(new Callback<>() {
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

	default EmptyFuture thenEmpty(Callable1<T> runnable) {
		return AsyncUtils.controllableEmptyFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				AsyncUtils.thread(() -> {
					try {
						runnable.run(result);
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

	default <E> AsyncFuture<E> thenControllable(ControllableAsyncFuture.Callback<E> callback) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				AsyncUtils.controllableFuture(callback).addCallback(new Callback<>() {
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
		return AsyncUtils.controllableEmptyFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				AsyncUtils.controllableEmptyFuture(callback).addCallback(new EmptyFuture.Callback() {
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

	default T await() {
		AsyncUtils.await(this::isDone);

		var t = getThrowable();

		if(t != null) {
			throw t instanceof RuntimeException runtimeException
					? runtimeException : new RuntimeException(t);
		}

		return getResult();
	}

	interface CallableResult<T, E> {
		E run(T input) throws Throwable;
	}

	interface Callable1<T> {
		void run(T input) throws Throwable;
	}

	interface Callback<T> {
		void onSuccess(@NotNull T result) throws Throwable;
		void onFailure(@NotNull Throwable t);
	}
}