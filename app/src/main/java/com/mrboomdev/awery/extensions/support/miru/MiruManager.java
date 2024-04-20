package com.mrboomdev.awery.extensions.support.miru;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MiruManager extends ExtensionsManager {
	private final Map<String, Extension> extensions = new HashMap<>();

	@Override
	public Extension getExtension(String id) {
		return extensions.get(id);
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return extensions.values();
	}

	@Override
	public String getName() {
		return "Miru";
	}

	@Override
	public String getId() {
		return "MIRU_JS";
	}
}