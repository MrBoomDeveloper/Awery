package com.mrboomdev.awery.extensions.support.internal;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.List;

public class InternalManager extends ExtensionsManager {
	public static final String MANAGER_ID = "INTERNAL";
	private final List<Extension> extensions = List.of(createExtension(new InternalProviders.Lists()));

	@NonNull
	private Extension createExtension(@NonNull InternalProviders.InternalProvider provider) {
		var extension = new Extension(provider.getId(), provider.getName(), "1.0.0", null);
		extension.addProvider(provider);
		provider.setup(this, extension);
		return extension;
	}

	@Override
	public Extension getExtension(String id) {
		return stream(extensions).filter(it -> id.equals(it.getId())).findAny().orElseThrow();
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