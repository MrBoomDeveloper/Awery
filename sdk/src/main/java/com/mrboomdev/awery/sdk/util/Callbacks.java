package com.mrboomdev.awery.sdk.util;

public class Callbacks {

	private Callbacks() {}

	public interface Callback1<T> {
		void run(T t);
	}

	public interface Callback2<T, E> {
		void run(T t, E e);
	}

	public interface Result<T> {
		T run();
	}

	public interface Result1<T, A> {
		T run(A a);
	}
	
	public interface Errorable<T, E> {
		void onResult(T t, E e);
	}
}