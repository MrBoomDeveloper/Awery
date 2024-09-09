package com.mrboomdev.awery.extensions.support.internal;

import static com.mrboomdev.awery.util.NiceUtils.find;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.async.EmptyFuture;

import java.util.Collection;
import java.util.List;

public class InternalManager extends ExtensionsManager {
	public static final String MANAGER_ID = "INTERNAL";
	private final List<__Extension> extensions = List.of(createExtension(new InternalProviders.Lists()));

	@NonNull
	private __Extension createExtension(@NonNull InternalProviders.InternalProvider provider) {
		var extension = new __Extension(provider.getName(), provider.getId(), "1.0.0", null);
		extension.addProvider(provider);
		provider.setup(this, extension);
		return extension;
	}

	@Override
	public EmptyFuture loadAllExtensions(Context context) {
		// Everything was loaded in the constructor
		return AsyncUtils.futureNow();
	}

	@Override
	public __Extension getExtension(String id) {
		return find(extensions, ext -> id.equals(ext.getId()));
	}

	@Override
	public Collection<__Extension> getAllExtensions() {
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