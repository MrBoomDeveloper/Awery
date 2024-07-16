package com.mrboomdev.awery.util.async;

import com.google.common.util.concurrent.FutureCallback;

import java.util.concurrent.Future;

public interface AsyncFuture<T> extends Future<T> {
	void addCallback(FutureCallback<T> callback);
}