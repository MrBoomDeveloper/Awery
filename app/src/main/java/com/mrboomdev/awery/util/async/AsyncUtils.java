package com.mrboomdev.awery.util.async;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mrboomdev.awery.sdk.util.Callbacks;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncUtils {
	private static final Object EMPTY_OBJECT = new Object();
	private static final Timer timer = new Timer();

	private static final ExecutorService threadsPool = new ThreadPoolExecutor(
			0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

	/**
	 * Run an action asynchronously. Prefer this method to the manual Thread object creation.
	 * @author MrBoomDev
	 */
	public static void thread(Runnable action) {
		threadsPool.execute(action);
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
	public static <T> ControllableAsyncFuture<T> controllableFuture(@NonNull ControllableAsyncFutureCallback<T> callback) {
		// Since we cannot directly access an thread from the queue we should to just patiently wait.
		var theThread = new AtomicReference<Thread>();
		var interrupt = new AtomicBoolean();

		var future = new ControllableAsyncFuture<T>() {
			private final Queue<FutureCallback<T>> callbacks = new ArrayDeque<>();
			private T result;
			private Throwable throwable;
			private boolean isCancelled;

			@Override
			public void complete(T result) {
				this.result = result;

				FutureCallback<T> nextCallback;
				while((nextCallback = callbacks.poll()) != null) {
					nextCallback.onSuccess(result);
				}
			}

			@Override
			public void fail(Throwable throwable) {
				this.throwable = throwable;

				FutureCallback<T> nextCallback;
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

				fail(new ExecutionException("Cancelled", null));
				return true;
			}

			@Override
			public void addCallback(FutureCallback<T> callback) {
				if(isDone()) {
					if(result != null) callback.onSuccess(result);
					if(throwable != null) callback.onFailure(throwable);
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
				return result != null || throwable != null;
			}

			@Override
			public T get() {
				return get(-1, null);
			}

			@Override
			public T get(long timeout, TimeUnit unit) {
				if(!isDone()) {
					await(this::isDone, unit != null ? unit.toMillis(timeout) : timeout);
				}

				return result;
			}
		};

		thread(() -> {
			theThread.set(Thread.currentThread());

			if(interrupt.get()) {
				theThread.get().interrupt();
			}

			callback.start(future);
		});

		return future;
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> futureFailNow(Throwable throwable) {
		return new AsyncFutureNow<>() {

			@Override
			public void addCallback(FutureCallback<T> callback) {
				callback.onFailure(throwable);
			}

			@Override
			public T get() {
				return null;
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> futureNow(T result) {
		return new AsyncFutureNow<>() {

			@Override
			public void addCallback(FutureCallback<T> callback) {
				callback.onSuccess(result);
			}

			@Override
			public T get() {
				return result;
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> thread(Callable<T> callable) {
		var future = Futures.submit(callable, threadsPool);

		return new AsyncFuture<>() {
			@Override
			public void addCallback(FutureCallback<T> callback) {
				Futures.addCallback(future, callback, threadsPool);
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
			public T get() throws ExecutionException, InterruptedException {
				return future.get();
			}

			@Override
			public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
				return future.get(timeout, unit);
			}
		};
	}

	/**
	 * Waits until the callback returns true
	 * @author MrBoomDev
	 */
	public static void await(@NonNull Callbacks.Result<Boolean> callback) {
		while(!callback.run());
	}

	public static void await(@NonNull Callbacks.Result<Boolean> callback, long maxDurationMs) {
		if(maxDurationMs == -1) {
			await(callback);
			return;
		}

		var isCancelled = new AtomicBoolean(false);
		var timer = runDelayed(() -> isCancelled.set(true), maxDurationMs);

		while(!callback.run() && !isCancelled.get());
		timer.cancel();
	}

	/**
	 * Waits until the callback returns non null
	 * @author MrBoomDev
	 */
	public static <T> T awaitNonNull(@NonNull Callbacks.Result<T> callback) {
		T result;

		do {
			result = callback.run();
		} while(result == null);

		return result;
	}

	/**
	 * Waits until the breaker would be called
	 * @param <T> The result value type
	 * @author MrBoomDev
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T awaitResult(@NonNull Callbacks.Callback1<Callbacks.Callback1<T>> breaker) {
		var resultWrapper = new AtomicReference<>();
		var result = EMPTY_OBJECT;

		breaker.run(resultWrapper::set);

		do {
			result = resultWrapper.get();
		} while(result == EMPTY_OBJECT);

		return (T) result;
	}
}