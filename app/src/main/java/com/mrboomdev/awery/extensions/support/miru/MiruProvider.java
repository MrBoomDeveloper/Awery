package com.mrboomdev.awery.extensions.support.miru;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.Collection;
import java.util.List;

public class MiruProvider extends ExtensionProvider {
	private final List<Integer> FEATURES = List.of(FEATURE_WATCH_MEDIA, FEATURE_READ_MEDIA);

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getName() {
		return null;
	}
}