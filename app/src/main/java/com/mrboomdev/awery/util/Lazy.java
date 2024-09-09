package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public abstract class Lazy<T> {
	protected T value;

	public T getIfInitialized() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	@NonNull
	@Contract(value = "_ -> new", pure = true)
	public static <T> Basic<T> createBasic(InitCallback<T> initCallback) {
		return new Basic<>() {
			@Override
			protected T init() {
				return initCallback.initValue();
			}
		};
	}

	@NonNull
	@Contract(value = "_ -> new", pure = true)
	public static <R, T extends Throwable> Hard<R, T> createHard(InitCallbackThrowable<R, T> initCallback) {
		return new Hard<>() {
			@Override
			protected R init() throws T {
				return initCallback.initValue();
			}
		};
	}

	@NonNull
	@Contract(value = "_ -> new", pure = true)
	public static <R, T extends Throwable> Hard<R, T> createHardNever(GetThrowable<T> getThrowable) {
		return new Hard<>() {
			@Override
			protected R init() throws T {
				throw getThrowable.getThrowable();
			}
		};
	}

	public interface GetThrowable<T extends Throwable> {
		T getThrowable();
	}

	public interface InitCallback<T> {
		T initValue();
	}

	public interface InitCallbackThrowable<R, T extends Throwable> {
		R initValue() throws T;
	}

	public static abstract class Basic<T> extends Lazy<T> {
		protected abstract T init();

		public T get() {
			if(value != null) {
				return value;
			}

			synchronized(this) {
				if(value != null) {
					return value;
				}

				return value = init();
			}
		}
	}

	public static abstract class Hard<R, T extends Throwable> extends Lazy<R> {
		protected abstract R init() throws T;

		public R get() throws T {
			if(value != null) {
				return value;
			}

			synchronized(this) {
				if(value != null) {
					return value;
				}

				return value = init();
			}
		}
	}
}