package com.mrboomdev.awery.extensions.support.aweryjs;

import androidx.annotation.NonNull;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValuePrimitive;
import com.caoccao.javet.values.reference.IV8ValuePromise;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueError;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValuePromise;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.JsException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AweryJsProvider extends ExtensionProvider {
	private final AweryJsManager manager;
	private final Map<String, ?> score;

	public AweryJsProvider(AweryJsManager manager, Map<String, ?> scope) {
		this.manager = manager;
		this.score = scope;
	}

	@Override
	public ExtensionsManager getManager() {
		return manager;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getFeatures() {
		return new HashSet<>((List<String>) get("features").await());
	}

	@Override
	public AsyncFuture<String> getChangelog() {
		return get("changelog");
	}

	@Override
	public String getId() {
		return (String) get("id").await();
	}

	@Override
	public AdultContent getAdultContentMode() {
		return AdultContent.valueOf((String) get("adultContent").await());
	}

	@SuppressWarnings("unchecked")
	@NonNull
	private <T> AsyncFuture<T> get(String name) {
		try {
			var it = score.get(name);

			if(it == null) {
				if(name.startsWith("get")) {
					return AsyncUtils.futureNow(null);
				}

				return get("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
			}

			if(it instanceof V8ValuePrimitive<?> primitive) {
				return (AsyncFuture<T>) AsyncUtils.futureNow(primitive.getValue());
			}

			if(it instanceof V8ValueArray array) {
				var list = new ArrayList<>(array.getLength());

				for(int i = 0; i < array.getLength(); i++) {
					list.set(i, array.get(i));
				}

				return (AsyncFuture<T>) AsyncUtils.futureNow(list);
			}

			if(it instanceof V8ValueFunction function) {
				return AsyncUtils.futureNow(function.callObject(null));
			}

			if(it instanceof V8ValuePromise promise) {
				return AsyncUtils.controllableFuture(future -> promise.register(new IV8ValuePromise.IListener() {
					@Override
					public void onCatch(V8Value v8Value) {
						onRejected(v8Value);
					}

					@Override
					public void onFulfilled(V8Value v8Value) {
						if(v8Value instanceof V8ValuePrimitive<?> primitive) {
							try {
								future.complete((T) primitive);
							} catch(Throwable e) {
								future.fail(e);
							}
						}
					}

					@Override
					public void onRejected(V8Value v8Value) {
						if(v8Value instanceof V8ValueError error) {
							try {
								future.fail(new JsException(error.getMessage(), new JsException(error.getStack())));
							} catch(JavetException ignored) {}
						} else {
							future.fail(new JsException(String.valueOf(v8Value)));
						}
					}
				}));
			}

			return (AsyncFuture<T>) AsyncUtils.futureNow(it);
		} catch(JavetException e) {
			throw new RuntimeException(e);
		}
	}
}