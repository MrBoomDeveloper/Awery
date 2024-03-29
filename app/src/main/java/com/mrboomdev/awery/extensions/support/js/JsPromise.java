package com.mrboomdev.awery.extensions.support.js;

import com.mrboomdev.awery.util.Callbacks;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("unused")
public class JsPromise {
	private boolean didCallThen, didCallCatch;
	private final Scriptable scope;
	private Callbacks.Result1<Object, Object> resolve;
	private Callbacks.Result1<Object, Object> reject;
	private Object thenResult, catchResult;
	private Object result, error;

	public JsPromise(Scriptable scope) {
		this.scope = scope;
	}

	public void resolve(Object object) {
		this.result = object;

		if(resolve != null) {
			thenResult = resolve.run(object);
		}
	}

	public void reject(Object object) {
		this.error = object;

		if(reject != null) {
			catchResult = reject.run(object);
		}
	}

	public JsPromise jsFunction_then(NativeFunction callback) {
		return then(t -> callback.call(Context.getCurrentContext(), scope, null, new Object[] { t }));
	}

	public JsPromise jsFunction_catchException(Function callback) {
		return catchException(e -> callback.call(Context.getCurrentContext(), scope, scope, new Object[] { e }));
	}

	public JsPromise then(Callbacks.Result1<Object, Object> callback) {
		/*if(didCallThen) {
			var promise = new JsPromise(scope);
			promise.then(callback);
			return promise;
		}*/

		this.didCallThen = true;
		this.resolve = callback;

		if(result != null) {
			this.resolve(result);
		}

		return this;
	}

	public JsPromise catchException(Callbacks.Result1<Object, Object> callback) {
		/*if(didCallCatch) {
			var promise = new JsPromise(scope);
			promise.catchException(callback);
			return promise;
		}*/

		this.didCallCatch = true;
		this.reject = callback;

		if(error != null) {
			this.reject(error);
		}

		return this;
	}
}