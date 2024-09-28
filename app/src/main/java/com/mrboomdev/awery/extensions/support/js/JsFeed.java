package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogFeed;

import org.mozilla.javascript.NativeObject;

public class JsFeed {

	@NonNull
	public static CatalogFeed fromJs(@NonNull JsProvider provider, @NonNull NativeObject o) {
		var feed = new CatalogFeed();
		feed.title = stringFromJs(o.get("title", o));
		feed.sourceFeed = stringFromJs(o.get("sourceFeed", o));
		feed.sourceManager = JsManager.MANAGER_ID;
		feed.providerId = provider.id;
		return feed;
	}
}