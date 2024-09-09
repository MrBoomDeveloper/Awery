package com.mrboomdev.awery.util.async;

import static com.mrboomdev.awery.util.NiceUtils.asRuntimeException;

import java.util.concurrent.Callable;

public abstract class EmptyFuture extends BaseFuture {
	public abstract void addCallback(Callback callback);

	public void addCallback(Runnable callback) {
		addCallback(new Callback() {
			@Override
			public void onFinally() {
				callback.run();
			}
		});
	}

	protected abstract boolean runInNewThread();

	public EmptyFuture thenEmpty(Callable1 runnable) {
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
		}), !runInNewThread());
	}

	public <E> AsyncFuture<E> then(Callable<E> runnable) {
		return AsyncUtils.controllableFuture(future -> addCallback(new Callback() {
			@Override
			public void onSuccess() {
				(runInNewThread() ? AsyncUtils.thread(() -> {
					try {
						return runnable.call();
					} catch(Throwable e) {
						throw asRuntimeException(e);
					}
				}) : new AsyncFutureNow<E>() {
					private E nextResult;
					private Throwable t;

					{
						try {
							nextResult = runnable.call();
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
				}).addCallback(new AsyncFuture.Callback<>() {
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
		}), !runInNewThread());
	}

	public <E> AsyncFuture<E> thenControllable(ControllableAsyncFuture.Callback<E> callback) {
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

	public EmptyFuture thenEmptyControllable(ControllableEmptyFuture.Callback callback) {
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

	public void await() {
		AsyncUtils.await(this::isDone);

		var t = getThrowable();

		if(t != null) {
			throw asRuntimeException(t);
		}
	}

	public interface Callable1 {
		void run() throws Throwable;
	}

	public interface Callback {

		/**
		 * Note: You have to manually call {@link #onFinally()} if you're going to override this method.
		 */
		default void onSuccess() throws Throwable {
			onFinally();
		}

		/**
		 * Note: You have to manually call {@link #onFinally()} if you're going to override this method.
		 */
		default void onFailure(Throwable t) {
			onFinally();
		}

		/**
		 * Don't forget call super in
		 * {@link #onSuccess()} and {@link #onFailure(Throwable)}
		 * or else this method won't be called
		 */
		default void onFinally() {}
	}
}