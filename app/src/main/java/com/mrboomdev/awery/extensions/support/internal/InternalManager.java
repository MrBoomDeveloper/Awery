package com.mrboomdev.awery.extensions.support.internal;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InternalManager extends ExtensionsManager {
	public static final String MANAGER_ID = "INTERNAL";
	private final List<Extension> extensions = List.of(createExtension("Lists", new InternalProviders.Lists()));

	@NonNull
	private Extension createExtension(String name, ExtensionProvider provider) {
		var extension = new Extension(name, name, "1.0.0", null);
		extension.addProvider(provider);
		return extension;
	}

	@Override
	public Extension getExtension(String id) {
		return null;
	}

	@Override
	public Collection<Extension> getAllExtensions() {
		return extensions;
	}

	@Override
	public String getName() {
		return "You're actually not supposed to see this text ._.";
	}

	@Override
	public String getId() {
		return MANAGER_ID;
	}
}