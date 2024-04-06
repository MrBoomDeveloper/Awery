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

		authorName = o.has("authorName", o) ? o.get("authorName").toString() : null;
		authorAvatar = o.has("authorAvatar", o) ? o.get("authorAvatar").toString() : null;
		text = o.has("text", o) ? o.get("text").toString() : null;
		date = o.has("date", o) ? o.get("date").toString() : null;

		likes = o.has("likes", o) ? ((Number) o.get("likes")).intValue() : CatalogComment.DISABLED;
		dislikes = o.has("dislikes", o) ? ((Number) o.get("dislikes")).intValue() : CatalogComment.DISABLED;
		comments = o.has("comments", o) ? ((Number) o.get("comments")).intValue() : CatalogComment.DISABLED;
		votes = o.has("votes", o) ? ((Number) o.get("votes")).intValue() : null;

		canComment = o.has("canComment", o) && (Boolean) o.get("canComment", o);
		hasNextPage = o.has("hasNextPage", o) && (Boolean) o.get("hasNextPage", o);

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