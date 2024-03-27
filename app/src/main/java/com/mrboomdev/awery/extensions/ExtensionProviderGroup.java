package com.mrboomdev.awery.extensions;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
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
	private final boolean areAllWithSameName;

	/**
	 * @param name A human-readable name of this group
	 * @param providers A list of {@link ExtensionProvider}
	 * @author MrBoomDev
	 */
	public ExtensionProviderGroup(String name, @NonNull List<ExtensionProvider> providers) {
		this.providers = providers;
		this.name = name;

		boolean areAllWithSameName = true;
		String lastName = null;

		for(var item : providers) {
			if(lastName == null) {
				lastName = item.getName();
				continue;
			}

			if(lastName.equals(item.getName())) {
				continue;
			}

			areAllWithSameName = false;
			break;
		}

		this.areAllWithSameName = areAllWithSameName;
	}

	/**
	 * @return True if all {@link ExtensionProvider} in this group have the same name
	 * @author MrBoomDev
	 */
	public boolean areAllWithSameName() {
		return areAllWithSameName;
	}

	/**
	 * @return The list of {@link ExtensionProvider}
	 * @author MrBoomDev
	 */
	public Collection<ExtensionProvider> getProviders() {
		return providers;
	}

	@Override
	public Collection<Integer> getFeatures() {
		return Collections.emptyList();
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