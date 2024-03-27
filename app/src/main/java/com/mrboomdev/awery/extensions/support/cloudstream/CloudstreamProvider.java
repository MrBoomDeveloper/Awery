package com.mrboomdev.awery.extensions.support.cloudstream;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.Collection;
import java.util.List;

public class CloudstreamProvider extends ExtensionProvider {
	private final List<Integer> FEATURES = List.of(FEATURE_WATCH_MEDIA);

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getName() {
		return "cloudstream";
	}
}