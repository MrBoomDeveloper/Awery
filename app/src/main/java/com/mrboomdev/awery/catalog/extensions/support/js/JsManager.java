package com.mrboomdev.awery.catalog.extensions.support.js;

import com.mrboomdev.awery.catalog.extensions.Extension;
import com.mrboomdev.awery.catalog.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.Collections;

public class JsManager extends ExtensionsManager {

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