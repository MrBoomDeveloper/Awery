package com.mrboomdev.awery.extensions.support.cloudstream;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.__ExtensionProvider;

import java.util.Set;

public abstract class CloudstreamProvider extends __ExtensionProvider {
	private static final Set<String> FEATURES = Set.of(ExtensionConstants.FEATURE_MEDIA_WATCH);

	public CloudstreamProvider(__Extension extension) {
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