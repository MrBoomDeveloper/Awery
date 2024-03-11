package com.mrboomdev.awery.catalog.extensions;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.util.TranslationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExtensionProviderGroup extends ExtensionProvider {
	private final ExtensionProvider[] providers;
	private final String name;

	public ExtensionProviderGroup(String name, ExtensionProvider... providers) {
		this.providers = providers;
		this.name = name;
	}

	public ExtensionProvider[] getProviders() {
		return providers;
	}

	@NonNull
	public static Map<String, Map<String, ExtensionProvider>> groupByLang(@NonNull Collection<ExtensionProvider> providers) {
		var result = new HashMap<String, Map<String, ExtensionProvider>>();

		for(var provider : providers) {
			var lang = TranslationUtil.getTranslatedLangName(AweryApp.getAnyContext(), provider.getLang());
			var name = provider.getName();

			if(result.containsKey(name)) {
				var langs = result.get(name);
				langs.put(lang, provider);
			} else {
				var langs = new HashMap<String, ExtensionProvider>();
				langs.put(lang, provider);
				result.put(name, langs);
			}
		}

		return result;
	}

	@Override
	public String getName() {
		return null;
	}
}