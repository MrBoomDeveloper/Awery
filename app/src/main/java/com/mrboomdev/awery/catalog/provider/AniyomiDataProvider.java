package com.mrboomdev.awery.catalog.provider;

import android.content.pm.PackageInfo;

public class AniyomiDataProvider extends DataProvider {
	private final PackageInfo pkg;

	public AniyomiDataProvider(PackageInfo pkg) {
		this.pkg = pkg;
	}
}