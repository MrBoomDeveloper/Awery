package com.mrboomdev.awery.extensions.support.cloudstream;

import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionsManager;

import java.util.Collection;
import java.util.Collections;

public class CloudstreamManager extends ExtensionsManager {

	@Override
	public __Extension getExtension(String id) {
		return null;
	}

	@Override
	public Collection<__Extension> getAllExtensions() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "CloudStream";
	}

	@Override
	public String getId() {
		return "CLOUDSTREAM_JAVA";
	}
}