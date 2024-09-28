package com.mrboomdev.awery.extensions.support.cloudstream;

import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;

import java.util.Set;

public abstract class CloudstreamProvider extends ExtensionProvider {
	private final Set<String> FEATURES = Set.of(FEATURE_MEDIA_WATCH);

	public CloudstreamProvider(Extension extension) {
		super(extension);
	}

	@Override
	public Set<String> getFeatures() {
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