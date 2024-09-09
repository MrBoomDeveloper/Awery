package com.mrboomdev.awery.util.async;

import static com.mrboomdev.awery.util.NiceUtils.asRuntimeException;

public abstract class AsyncFuture<T> extends BaseFuture {
	public abstract void addCallback(Callback<T> callback);

	public abstract T getResult();

	public void addFailureCallback() {
		addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) throws Throwable {}

			@Override
			public void onFailure(Throwable t) {

			}
		});
	}

	@Override
	protected boolean hasResult() {
		return getResult() != null || getThrowable() != null;
	}

	protected abstract boolean runInNewThread();

	public <E> AsyncFuture<E> then(CallableResult<T, E> runnable) {
		return then(runnable, !runInNewThread());
	}

	public <E> AsyncFuture<E> then(CallableResult<T, E> runnable, boolean async) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				(runInNewThread() ? AsyncUtils.thread(() -> {
					try {
						return runnable.run(result);
					} catch(Throwable e) {
						throw asRuntimeException(e);
					}
				}) : new AsyncFutureNow<E>() {
					private E nextResult;
					private Throwable t;

					{
						try {
							nextResult = runnable.run(result);
						} catch(Throwable e) {
							t = e;
						}
					}

					@Override
					public Throwable getThrowable() {
						return t;
					}

					@Override
					public E getResult() {
						return nextResult;
					}

					@Override
					protected boolean runInNewThread() {
						return true;
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
		}), async);
	}

	public EmptyFuture thenEmpty(Callable1<T> runnable) {
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
		}), !runInNewThread());
	}

	public <E> AsyncFuture<E> thenControllable(ControllableResultCallback<T, E> callback) {
		return thenControllable(callback, !runInNewThread());
	}

	public interface ControllableResultCallback<I, O> {
		void start(I input, ControllableAsyncFuture<O> future) throws Throwable;
	}

	public <E> AsyncFuture<E> thenControllable(ControllableResultCallback<T, E> callback, boolean newThread) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				AsyncUtils.controllableFuture((ControllableAsyncFuture.Callback<E>) future1 -> callback.start(result, future1))
						.addCallback(new Callback<>() {
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
		}), newThread);
	}

	public EmptyFuture thenEmptyControllable(ControllableEmptyFuture.Callback callback) {
		return AsyncUtils.controllableEmptyFuture(future -> addCallback(new Callback<>() {
			@Override
			public void onSuccess(T result) {
				AsyncUtils.controllableEmptyFuture(callback, !runInNewThread())
						.addCallback(new EmptyFuture.Callback() {
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
		}), !runInNewThread());
	}

	public interface DefaultValueCallback<T> {
		T getDefault();
	}

	public T awaitCatch(DefaultValueCallback<T> defaultValueCallback) {
		try {
			return await();
		} catch(Throwable e) {
			return defaultValueCallback.getDefault();
		}
	}

	public T await() throws Throwable {
		AsyncUtils.await(this::isDone);

		var t = getThrowable();

		if(t != null) {
			throw t;
		}

		return getResult();
	}

	public interface CallableResult<T, E> {
		E run(T input) throws Throwable;
	}

	public interface Callable1<T> {
		void run(T input) throws Throwable;
	}

	public interface Callback<T> {
		/**
		 * Note: You have to manually call {@link #onFinally()} if you're going to override this method.
		 */
		default void onSuccess(T result) throws Throwable {
			onFinally();
		}

		/**
		 * Note: You have to manually call {@link #onFinally()} if you're going to override this method.
		 */
		default void onFailure(Throwable t) {
			onFinally();
		}

		default void onFinally() {}
	}
}