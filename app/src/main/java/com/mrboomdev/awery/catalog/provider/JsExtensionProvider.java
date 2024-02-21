package com.mrboomdev.awery.catalog.provider;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogMedia;

import org.mozilla.javascript.Context;

import java.util.List;

public class JsExtensionProvider extends ExtensionProvider {

	@Override
	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<CatalogEpisode>> callback) {
		super.getEpisodes(page, media, callback);
	}

	public JsExtensionProvider(String script) {
		var context = Context.enter();
		var scope = context.initSafeStandardObjects();
		context.evaluateString(scope, script, null, 1,null);
	}

	@Override
	public String getName() {
		return "JsExtensionProvider";
	}
}