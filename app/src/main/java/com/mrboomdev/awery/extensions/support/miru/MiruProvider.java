package com.mrboomdev.awery.extensions.support.miru;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Set;

public abstract class MiruProvider extends __ExtensionProvider {
	private static final Set<String> FEATURES = Set.of(
			ExtensionConstants.FEATURE_MEDIA_WATCH,
			ExtensionConstants.FEATURE_MEDIA_READ);

	public MiruProvider(__Extension extension) {
		super(extension);
	}

	@Override
	public ExtensionsManager getManager() {
		return null;
	}

	@Override
	public Set<String> getFeatures() {
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