package com.mrboomdev.awery.extensions.support.js;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogComment;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class JsComment extends CatalogComment {
	private final ScriptableObject customData;

	protected JsComment(@NonNull ScriptableObject o) {
		if(o.has("items", o)) {
			for(var arrayItem : (NativeArray) o.get("items", o)) {
				var jsItem = (NativeObject) arrayItem;
				items.add(new JsComment(jsItem));
			}
		}

		authorName = o.has("authorName", o) ? JsBridge.fromJs(o.get("authorName", o), String.class) : null;
		authorAvatar = o.has("authorAvatar", o) ? JsBridge.fromJs(o.get("authorAvatar", o), String.class) : null;
		text = o.has("text", o) ? JsBridge.fromJs(o.get("text", o), String.class) : null;
		date = o.has("date", o) ? JsBridge.fromJs(o.get("date", o), String.class) : null;

		likes = o.has("likes", o) ? JsBridge.intFromJs(o.get("likes", o)) : CatalogComment.DISABLED;
		dislikes = o.has("dislikes", o) ? JsBridge.intFromJs(o.get("dislikes", o)) : CatalogComment.DISABLED;
		comments = o.has("comments", o) ? JsBridge.intFromJs(o.get("comments", o)) : CatalogComment.DISABLED;
		votes = o.has("votes", o) ? JsBridge.fromJs(o.get("votes", o), Integer.class) : null;

		canComment = o.has("canComment", o) && JsBridge.booleanFromJs(o.get("canComment", o));
		hasNextPage = o.has("hasNextPage", o) && JsBridge.booleanFromJs(o.get("hasNextPage", o));

		this.customData = o;
	}

	public static Scriptable createJsComment(Context context, Scriptable scope, CatalogComment comment) {
		if(comment == null) return null;

		if(comment instanceof JsComment jsComment) {
			return jsComment.customData;
		}

		var o = context.newObject(scope);

		o.put("authorName", o, comment.authorName);
		o.put("authorAvatar", o, comment.authorAvatar);
		o.put("text", o, comment.text);
		o.put("date", o, comment.date);

		o.put("likes", o, comment.likes);
		o.put("dislikes", o, comment.dislikes);
		o.put("votes", o, comment.votes);

		o.put("canComment", o, comment.canComment);
		o.put("hasNextPage", o, comment.hasNextPage);

		return o;
	}

	@JSGetter("customData")
	public ScriptableObject getCustomData() {
		return customData;
	}
}