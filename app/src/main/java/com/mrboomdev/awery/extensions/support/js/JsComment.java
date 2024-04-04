package com.mrboomdev.awery.extensions.support.js;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogComment;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class JsComment extends CatalogComment {
	private final ScriptableObject originalData;

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

		this.originalData = o;
	}

	@JSGetter("customData")
	protected ScriptableObject getCustomData() {
		return originalData;
	}
}