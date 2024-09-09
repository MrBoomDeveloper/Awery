package com.mrboomdev.awery.ext.source;

import com.mrboomdev.awery.ext.constants.AdultContentMode;
import com.mrboomdev.awery.ext.data.Image;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class Extension {

	public abstract String getName();

	public abstract String getVersion();

	public abstract ExtensionProvider getProvider();

	@NotNull
	public abstract Collection<String> getFeatures();

	public final boolean hasFeature(String feature) {
		for(var item : getFeatures()) {
			if(item.equals(feature)) return true;
		}

		return false;
	}

	public abstract AdultContentMode getAdultContentMode();

	@NotNull
	public abstract String getId();

	public abstract Image getIcon();

	@Nullable
	public abstract Throwable getError();
}