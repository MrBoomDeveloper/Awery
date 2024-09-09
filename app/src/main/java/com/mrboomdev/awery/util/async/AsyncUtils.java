package com.mrboomdev.awery.util.async;

import static com.mrboomdev.awery.app.Lifecycle.isMainThread;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.EMPTY_OBJECT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.util.exceptions.CancelledException;

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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncUtils {
	private static final Timer timer = new Timer();

	private static final ExecutorService threadsPool = new ThreadPoolExecutor(
			0, Integer.MAX_VALUE,
			60L, TimeUnit.SECONDS,
			new SynchronousQueue<>(),
			new ThreadFactory() {
				private final ThreadGroup group = Thread.currentThread().getThreadGroup();
				private final AtomicInteger threadNumber = new AtomicInteger(1);

				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(group, r, "AsyncFuture-" + threadNumber.getAndIncrement(), 0);

					if(t.isDaemon()) {
						t.setDaemon(false);
					}

					if(t.getPriority() != Thread.NORM_PRIORITY) {
						t.setPriority(Thread.NORM_PRIORITY);
					}

					return t;
				}
			});

	/**
	 * Run an action asynchronously. Prefer this method to the manual Thread object creation.
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
	public static EmptyFuture thread(ThreadEmptyRunnable action) {
		return controllableEmptyFuture(future -> {
			action.run();
			future.complete();
		});
	}

	public interface ThreadEmptyRunnable {
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
		return controllableFuture(callback, true);
	}

	@NonNull
	public static <T> ControllableAsyncFuture<T> controllableFuture(@NonNull ControllableAsyncFuture.Callback<T> callback, boolean async) {
		// Since we cannot directly access an thread from the queue we should to just patiently wait.
		var theThread = new AtomicReference<Thread>();
		var interrupt = new AtomicBoolean();

		var future = new BaseControllableAsyncFuture<T>() {
			@Override
			protected void interrupt() {
				if(!async) {
					return;
				}

				interrupt.set(true);

				if(theThread.get() != null) {
					theThread.get().interrupt();
				}
			}

			@Override
			protected boolean runInNewThread() {
				return async;
			}
		};

		if(!async) {
			try {
				callback.start(future);
			} catch(Throwable t) {
				future.fail(t);
			}
		} else {
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
		}

		return future;
	}

	@NonNull
	public static ControllableEmptyFuture controllableEmptyFuture(@NonNull ControllableEmptyFuture.Callback callback) {
		return controllableEmptyFuture(callback, true);
	}

	@NonNull
	public static ControllableEmptyFuture controllableEmptyFuture(@NonNull ControllableEmptyFuture.Callback callback, boolean async) {
		// Since we cannot directly access an thread from the queue we should to just patiently wait.
		var theThread = new AtomicReference<Thread>();
		var interrupt = new AtomicBoolean();

		var future = new ControllableEmptyFuture() {
			private final Queue<EmptyFuture.Callback> callbacks = new ArrayDeque<>();
			private Throwable throwable;
			private boolean isCancelled, didDone;

			@Override
			public void complete() {
				if(hasResult()) return;
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
				if(hasResult()) return;
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
				if(throwable != null) {
					callback.onFailure(throwable);
				}

				if(didDone) {
					try {
						callback.onSuccess();
					} catch(Throwable e) {
						callback.onFailure(e);
						return;
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

			@Override
			protected boolean runInNewThread() {
				return async;
			}
		};

		if(!async) {
			try {
				callback.start(future);
			} catch(Throwable t) {
				future.fail(t);
			}
		} else {
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
		}

		return future;
	}

	private abstract static class BaseControllableAsyncFuture<T> extends ControllableAsyncFuture<T> {
		protected final Queue<AsyncFuture.Callback<T>> callbacks = new ArrayDeque<>();
		protected T result;
		protected Throwable throwable;
		protected boolean isCancelled;

		@Override
		public void complete(T result) {
			if(hasResult()) return;
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
			if(hasResult()) return;
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
				interrupt();
			}

			fail(new CancelledException());
			return true;
		}

		protected abstract void interrupt();

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
		protected boolean hasResult() {
			return getResult() != null || getThrowable() != null;
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
		public Throwable getThrowable() {
			return throwable;
		}
	}

	@NonNull
	@Contract("_ -> new")
	public static EmptyFuture emptyFutureFailNow(Throwable throwable) {
		return new EmptyFuture() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public Throwable getThrowable() {
				return throwable;
			}

			@Override
			public void addCallback(Callback callback) {
				callback.onFailure(throwable);
			}

			@Override
			protected boolean runInNewThread() {
				return false;
			}
		};
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

			@Override
			protected boolean runInNewThread() {
				return false;
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

			@Override
			protected boolean runInNewThread() {
				return false;
			}
		};
	}

	@NonNull
	public static EmptyFuture futureNow() {
		return new EmptyFuture() {
			private Throwable throwable;

			@Override
			public void addCallback(Callback callback) {
				try {
					callback.onSuccess();
				} catch(Throwable t) {
					this.throwable = t;
					callback.onFailure(t);
				}
			}

			@Override
			protected boolean runInNewThread() {
				return false;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public Throwable getThrowable() {
				return throwable;
			}
		};
	}

	public interface ThreadRunnable<T> {
		T run() throws Throwable;
	}

	@NonNull
	@Contract("_ -> new")
	public static <T> AsyncFuture<T> thread(ThreadRunnable<T> callable) {
		var future = Futures.submit(() -> {
			try {
				return callable.run();
			} catch(Throwable t) {
				throw t instanceof Exception e ? e : new ExecutionException(t);
			}
		}, threadsPool);

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
			protected boolean runInNewThread() {
				return true;
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

	public static <T> T awaitFromUiThread(Callable<T> callable) throws Exception {
		if(isMainThread()) {
			return callable.call();
		}

		var result = new AtomicReference<T>();
		var throwable = new AtomicReference<Exception>();

		runOnUiThread(() -> {
			try {
				result.set(callable.call());
			} catch(Exception t) {
				throwable.set(t);
			}
		});

		while(result.get() == null && throwable.get() == null);

		if(throwable.get() != null) {
			throw throwable.get();
		}

		return result.get();
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