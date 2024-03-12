package com.mrboomdev.awery.catalog.extensions;

import java.util.Collection;
import java.util.List;

/**
 * A group of {@link ExtensionProvider}
 * This class don't have to implement any method of {@link ExtensionProvider},
 * so please don't use it to retrieve any data
 * @author MrBoomDev
 */
public class ExtensionProviderGroup extends ExtensionProvider {
	private final List<ExtensionProvider> providers;
	private final String name;

	/**
	 * @param name A human-readable name of this group
	 * @param providers A list of {@link ExtensionProvider}
	 * @author MrBoomDev
	 */
	public ExtensionProviderGroup(String name, List<ExtensionProvider> providers) {
		this.providers = providers;
		this.name = name;
	}

	/**
	 * @return The list of {@link ExtensionProvider}
	 * @author MrBoomDev
	 */
	public Collection<ExtensionProvider> getProviders() {
		return providers;
	}

	/**
	 * @return A human-readable name of this group
	 * @author MrBoomDev
	 */
	@Override
	public String getName() {
		return name;
	}
}