package com.mrboomdev.awery.catalog.extensions.support.aniyomi;

import com.mrboomdev.awery.catalog.extensions.ExtensionsManager;

public class AniyomiManager extends ExtensionsManager<AniyomiProvider> {

	@Override
	public String getName() {
		return "Aniyomi";
	}

	@Override
	public String getId() {
		return "ANIYOMI_KOTLIN";
	}
}