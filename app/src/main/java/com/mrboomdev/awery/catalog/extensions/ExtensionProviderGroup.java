package com.mrboomdev.awery.catalog.extensions;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.util.TranslationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public List<ExtensionProvider> getProviders() {
		return providers;
	}

	@NonNull
	@Deprecated(forRemoval = true)
	public static Map<String, Map<String, ExtensionProvider>> groupByLang(@NonNull Collection<ExtensionProvider> providers) {
		var result = new HashMap<String, Map<String, ExtensionProvider>>();

		for(var provider : providers) {
			var lang = TranslationUtil.getTranslatedLangName(AweryApp.getAnyContext(), provider.getLang());
			var name = provider.getName();

			if(result.containsKey(name)) {
				var langs = result.get(name);
				if(langs == null) continue;

				langs.put(lang, provider);
			} else {
				var langs = new HashMap<String, ExtensionProvider>();
				langs.put(lang, provider);
				result.put(name, langs);
			}
		}

		return result;
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