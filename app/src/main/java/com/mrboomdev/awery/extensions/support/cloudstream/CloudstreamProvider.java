package com.mrboomdev.awery.extensions.support.cloudstream;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.Collection;
import java.util.List;

public abstract class CloudstreamProvider extends ExtensionProvider {
	private final List<Integer> FEATURES = List.of(FEATURE_MEDIA_WATCH);

	public CloudstreamProvider(Extension extension) {
		super(extension);
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getName() {
		return "cloudstream";
	}

	@Override
	public String getId() {
		return null;
	}
}