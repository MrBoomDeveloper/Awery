package com.mrboomdev.awery.extensions.support.js;

import com.mrboomdev.awery.util.Callbacks;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("unused")
public class JsPromise<T, E> {
	private final Scriptable scope;
	private Callbacks.Callback1<T> resolve;
	private Callbacks.Callback1<E> reject;
	private T result;
	private E error;

	public JsPromise(Scriptable scope) {
		this.scope = scope;
	}

	public void resolve(T object) {
		this.result = object;

		if(resolve != null) {
			resolve.run(object);
		}
	}

	public void reject(E object) {
		this.error = object;

		if(reject != null) {
			reject.run(object);
		}
	}

	public JsPromise<T, E> jsFunction_then(NativeFunction callback) {
		this.resolve = t -> callback.call(Context.getCurrentContext(), scope, null, new Object[] { t });
		return this;
	}

	public JsPromise<T, E> jsFunction_catchException(Function callback) {
		this.reject = e -> callback.call(Context.getCurrentContext(), scope, scope, new Object[] { e });
		return this;
	}

	public JsPromise<T, E> then(Callbacks.Callback1<T> callback) {
		this.resolve = callback;

		if(result != null) {
			this.resolve(result);
		}

		return this;
	}

	public JsPromise<T, E> catchException(Callbacks.Callback1<E> callback) {
		this.reject = callback;

		if(error != null) {
			this.reject(error);
		}

		return this;
	}
}