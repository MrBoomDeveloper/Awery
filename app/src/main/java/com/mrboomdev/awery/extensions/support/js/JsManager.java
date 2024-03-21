package com.mrboomdev.awery.extensions.support.js;

import android.content.Context;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.Collections;

public class JsManager extends ExtensionsManager {
	private org.mozilla.javascript.Context context;

	public JsManager() {
		new Thread(() -> {
			this.context = org.mozilla.javascript.Context.enter();
		}, "JsManager").start();
	}

	@Override
	public void init(Context context, String id) {
		super.init(context, id);
	}

	@Override
	public Extension getExtension(String id) {
		return null;
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "JavaScript";
	}

	@Override
	public String getId() {
		return "AWERY_JS";
	}
}