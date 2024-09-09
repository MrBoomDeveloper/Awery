/*
package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.longFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogVideo;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

public class JsEpisode extends CatalogVideo {
	private final NativeObject data;

	public JsEpisode(@NonNull NativeObject data) {
		super(stringFromJs(data.get("title", data)),
				null,
				stringFromJs(data.get("banner", data)),
				stringFromJs(data.get("description", data)),
				longFromJs(data.get("releaseDate", data)),
				floatFromJs(data.get("number", data)));

		this.data = data;
	}

	public static Scriptable getJsEpisode(Context context, Scriptable scope, CatalogVideo episode) {
		if(episode == null) return null;

		if(episode instanceof JsEpisode jsEpisode) {
			return jsEpisode.data;
		}

		var o = context.newObject(scope);
		o.put("title", o, episode.getTitle());
		o.put("description", o, episode.getDescription());
		o.put("number", o, episode.getNumber());
		o.put("id", o, episode.getId());
		return o;
	}
}*/