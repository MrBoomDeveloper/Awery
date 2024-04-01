package com.mrboomdev.awery.extensions.support.miru;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.Collection;
import java.util.List;

public class MiruProvider extends ExtensionProvider {
	private final List<Integer> FEATURES = List.of(FEATURE_MEDIA_WATCH, FEATURE_MEDIA_READ);

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