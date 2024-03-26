package com.mrboomdev.awery.extensions.support.js;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class JsProvider extends ExtensionProvider {
	private static final Object[] EMPTY_ARGS = new Object[0];
	private final ScriptableObject scope;
	private final String name;
	private final JsManager manager;
	protected final String id, version;

	public JsProvider(JsManager manager, @NonNull Context context, @NonNull String script) {
		this.manager = manager;
		this.scope = context.initSafeStandardObjects();

		context.evaluateString(scope, script, null, 1,null);

		if(scope.get("aweryManifest") instanceof Function fun) {
			var obj = (ScriptableObject) fun.call(context, scope, null, EMPTY_ARGS);
			this.name = (String) obj.get("title");
			this.id = (String) obj.get("id");
			this.version = (String) obj.get("version");
		} else {
			throw new IllegalStateException("aweryManifest is not a function or isn't defined!");
		}
	}

	@Override
	public String getName() {
		return name;
	}
}