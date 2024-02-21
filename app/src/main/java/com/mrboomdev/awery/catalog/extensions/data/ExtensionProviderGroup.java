package com.mrboomdev.awery.catalog.extensions.data;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.extensions.ExtensionProvider;
import com.mrboomdev.awery.util.TranslationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExtensionProviderGroup {

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
}