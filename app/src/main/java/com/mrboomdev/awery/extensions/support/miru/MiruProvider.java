package com.mrboomdev.awery.extensions.support.miru;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.List;

public class MiruProvider extends ExtensionProvider {
	private final List<Integer> FEATURES = List.of(FEATURE_MEDIA_WATCH, FEATURE_MEDIA_READ);

	public MiruProvider(Extension extension) {
		super(extension);
	}

	@Override
	public ExtensionsManager getManager() {
		return null;
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getId() {
		return null;
	}
}