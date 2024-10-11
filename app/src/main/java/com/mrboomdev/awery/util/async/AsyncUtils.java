package com.mrboomdev.awery.util.async;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mrboomdev.awery.util.exceptions.CancelledException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Deprecated(forRemoval = true)
public class AsyncUtils {
	private static final Object EMPTY_OBJECT = new Object();
	private static final Timer timer = new Timer();

	private static final ExecutorService threadsPool = new ThreadPoolExecutor(
			0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

	/**
	 * Run an action asynchronously. Prefer this method to the manual Thread object creation.
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
	public static EmptyFuture thread(ThreadRunnable action) {
		return controllableEmptyFuture(future -> {
			action.run();
			future.complete();
		});
	}

	public interface ThreadRunnable {
		void run() throws Throwable;
	}

	@Contract("_, _ -> new")
	public static TimerTask runDelayed(@NonNull Runnable runnable, @Range(from = 0, to = Long.MAX_VALUE) long delayMs) {
		return runDelayed(new TimerTask() {

			@Override
			public void run() {
				runnable.run();
			}
		}, delayMs);
	}

	public static TimerTask runDelayed(@NonNull TimerTask task, @Range(from = 0, to = Long.MAX_VALUE) long delayMs) {
		timer.schedule(task, delayMs);
		return task;
	}

	@NonNull
	public static <T> ControllableAsyncFuture<T> controllableFuture(@NonNull ControllableAsyncFuture.Callback<T> callback) {
		// Since we cannot directly access an thread from the queue we should to just patiently wait.
		var theThread = new AtomicReference<Thread>();
		var interrupt = new AtomicBoolean();

		var future = new ControllableAsyncFuture<T>() {
			private final Queue<AsyncFuture.Callback<T>> callbacks = new ArrayDeque<>();
			private T result;
			private Throwable throwable;
			private boolean isCancelled;

			@Override
			public void complete(T result) {
				this.result = result;

				AsyncFuture.Callback<T> nextCallback;
				while((nextCallback = callbacks.poll()) != null) {
					try {
						nextCallback.onSuccess(result);
					} catch(Throwable e) {
						nextCallback.onFailure(e);
					}
				}
			}

			@Override
			public void fail(Throwable throwable) {
				this.throwable = throwable;

				AsyncFuture.Callback<T> nextCallback;
				while((nextCallback = callbacks.poll()) != null) {
					nextCallback.onFailure(throwable);
				}
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if(isDone()) return false;
				isCancelled = true;

				if(mayInterruptIfRunning) {
					interrupt.set(true);

					if(theThread.get() != null) {
						theThread.get().interrupt();
					}
				}

				fail(new CancelledException());
				return true;
			}

			@Override
			public void addCallback(AsyncFuture.Callback<T> callback) {
				if(isDone()) {
					if(result != null) {
						try {
							callback.onSuccess(result);
						} catch(Throwable e) {
							callback.onFailure(e);
							return;
						}
					}

					if(throwable != null) {
						callback.onFailure(throwable);
					}

					return;
				}

				callbacks.add(callback);
			}

			@Override
			public T getResult() {
				return result;
			}

			@Override
			public boolean isCancelled() {
				return isCancelled;
			}

			@Override
			public boolean isDone() {
				return result != null || throwable != null;
			}

			@Nullable
			@Contract(pure = true)
			@Override
			public Throwable getThrowable() {
				return null;
			}
		};

		threadsPool.submit(() -> {
			theThread.set(Thread.currentThread());

			if(interrupt.get()) {
				theThread.get().interrupt();
			}

			try {
				callback.start(future);
			} catch(Throwable t) {
				future.fail(t);
			}
		});

		return future;
	}

	@NonNull
	public static ControllableEmptyFuture controllableEmptyFuture(@NonNull ControllableEmptyFuture.Callback callback) {
		// Since we cannot directly access an thread from the queue we should to just patiently wait.
		var theThread = new AtomicReference<Thread>();
		var interrupt = new AtomicBoolean();

		var future = new ControllableEmptyFuture() {
			private final Queue<EmptyFuture.Callback> callbacks = new ArrayDeque<>();
			private Throwable throwable;
			private boolean isCancelled, didDone;

			@Override
			public void complete() {
				this.didDone = true;

				EmptyFuture.Callback nextCallback;
				while((nextCallback = callbacks.poll()) != null) {
					try {
						nextCallback.onSuccess();
					} catch(Throwable e) {
						nextCallback.onFailure(e);
					}
				}
			}

			@Override
			public void fail(Throwable throwable) {
				this.throwable = throwable;

				EmptyFuture.Callback nextCallback;
				while((nextCallback = callbacks.poll()) != null) {
					nextCallback.onFailure(throwable);
				}
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if(isDone()) return false;
				isCancelled = true;

				if(mayInterruptIfRunning) {
					interrupt.set(true);

					if(theThread.get() != null) {
						theThread.get().interrupt();
					}
				}

				fail(new CancelledException());
				return true;
			}

			@Override
			public void addCallback(EmptyFuture.Callback callback) {
				if(isDone()) {
					if(didDone) {
						try {
							callback.onSuccess();
						} catch(Throwable e) {
							callback.onFailure(e);
							return;
						}
					}

					if(throwable != null) {
						callback.onFailure(throwable);
					}

					return;
				}

				callbacks.add(callback);
			}

			@Override
			public boolean isCancelled() {
				return isCancelled;
			}

			@Override
			public boolean isDone() {
				return didDone || throwable != null;
			}

			@Override
			public Throwable getThrowable() {
				return throwable;
			}
		};

		threadsPool.submit(() -> {
			theThread.set(Thread.currentThread());

			if(interrupt.get()) {
				theThread.get().interrupt();
			}

			try {
				callback.start(future);
			} catch(Throwable t) {
				future.fail(t);
			}
		});

		return future;
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> futureFailNow(Throwable throwable) {
		return new AsyncFutureNow<>() {

			@Override
			public Throwable getThrowable() {
				return throwable;
			}

			@Override
			public void addCallback(AsyncFuture.Callback<T> callback) {
				callback.onFailure(throwable);
			}

			@Override
			public T getResult() {
				return null;
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> futureNow(T result) {
		return new AsyncFutureNow<>() {
			private Throwable throwable;

			@Override
			public Throwable getThrowable() {
				return throwable;
			}

			@Override
			public void addCallback(AsyncFuture.Callback<T> callback) {
				try {
					callback.onSuccess(result);
				} catch(Throwable e) {
					this.throwable = e;
					callback.onFailure(e);
				}
			}

			@Override
			public T getResult() {
				return result;
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> thread(Callable<T> callable) {
		var future = Futures.submit(callable, threadsPool);

		return new AsyncFuture<>() {
			private Throwable throwable;
			private T value;

			@Override
			public void addCallback(AsyncFuture.Callback<T> callback) {
				Futures.addCallback(future, new FutureCallback<>() {
					@Override
					public void onSuccess(T result) {
						try {
							value = result;
							callback.onSuccess(result);
						} catch(Throwable e) {
							onFailure(e);
						}
					}

					@Override
					public void onFailure(@NonNull Throwable t) {
						throwable = t;
						callback.onFailure(t);
					}
				}, threadsPool);
			}

			@Override
			public T getResult() {
				return value;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return future.cancel(mayInterruptIfRunning);
			}

			@Override
			public boolean isCancelled() {
				return future.isCancelled();
			}

			@Override
			public boolean isDone() {
				return future.isDone();
			}

			@Override
			public Throwable getThrowable() {
				return throwable;
			}
		};
	}

	/**
	 * Waits until the callback returns true
	 * @author MrBoomDev
	 */
	public static void await(@NonNull Result<Boolean> callback) {
		while(!callback.run());
	}

	public interface Result<T> {
		T run();
	}

	public interface Callback1<T> {
		void run(T arg);
	}

	/**
	 * Waits until the breaker would be called
	 * @param <T> The result value type
	 * @author MrBoomDev
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T awaitResult(@NonNull Callback1<Callback1<T>> breaker) {
		var resultWrapper = new AtomicReference<>();
		var result = EMPTY_OBJECT;

		breaker.run(resultWrapper::set);

		do {
			result = resultWrapper.get();
		} while(result == EMPTY_OBJECT);

		return (T) result;
	}
}