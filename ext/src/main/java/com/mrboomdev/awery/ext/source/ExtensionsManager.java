package com.mrboomdev.awery.ext.source;

import com.mrboomdev.awery.ext.data.Progress;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.NoSuchElementException;

public abstract class ExtensionsManager {

	@NotNull
	public String getName() {
		return getId();
	}

	public abstract Progress getProgress();

	@NotNull
	public abstract String getId();

	public Extension getExtension(String id) throws NoSuchElementException {
		for(var extension : getAllExtensions()) {
			if(id.equals(extension.getId())) {
				return extension;
			}
		}

		throw new NoSuchElementException("No extension \"" + id + "\" was found!");
	}

	public abstract Extension installExtension(InputStream inputStream);

	/**
	 * @return {@code true} if uninstalled successfully
	 */
	public abstract boolean uninstallExtension(String id);

	public abstract Repository getRepository(String url);

	public abstract Extension loadExtension(String id);

	public abstract void unloadExtension(String id);

	public abstract void loadAllExtensions();

	@NotNull
	public abstract Collection<? extends Extension> getAllExtensions();
}