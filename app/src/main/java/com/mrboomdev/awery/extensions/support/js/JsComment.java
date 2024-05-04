package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.fromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.intFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.isNull;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogComment;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSGetter;

public class JsComment extends CatalogComment {
	private final NativeObject customData;

	protected JsComment(@NonNull NativeObject o) {
		if(o.has("items", o)) {
			for(var arrayItem : (NativeArray) o.get("items", o)) {
				var jsItem = (NativeObject) arrayItem;
				items.add(new JsComment(jsItem));
			}
		}

		authorName = stringFromJs(o.get("authorName", o));
		authorAvatar = stringFromJs(o.get("authorAvatar", o));
		text = stringFromJs(o.get("text", o));
		date = stringFromJs(o.get("date", o));

		likes = o.has("likes", o) ? intFromJs(o.get("likes", o)) : CatalogComment.DISABLED;
		dislikes = o.has("dislikes", o) ? intFromJs(o.get("dislikes", o)) : CatalogComment.DISABLED;
		comments = o.has("comments", o) ? intFromJs(o.get("comments", o)) : CatalogComment.DISABLED;
		votes = o.has("votes", o) ? fromJs(o.get("votes", o), Integer.class) : null;
		voteState = intFromJs(o.get("voteState", o));
		id = stringFromJs(o.get("id", o));

		canComment = booleanFromJs(o.get("canComment", o));
		hasNextPage = booleanFromJs(o.get("hasNextPage", o));

		this.customData = o;
	}

	public static Scriptable createJsComment(Context context, Scriptable scope, CatalogComment comment) {
		if(comment == null) return null;
		var o = context.newObject(scope);

		if(comment instanceof JsComment jsComment) {
			for(var prop : jsComment.customData.entrySet()) {
				var key = prop.getKey();
				if(isNull(key)) continue;

				o.put(prop.getKey().toString(), o, prop.getValue());
			}
		}

		o.put("authorName", o, comment.authorName);
		o.put("authorAvatar", o, comment.authorAvatar);
		o.put("text", o, comment.text);
		o.put("date", o, comment.date);
		o.put("id", o, comment.id);

		o.put("likes", o, comment.likes);
		o.put("dislikes", o, comment.dislikes);
		o.put("votes", o, comment.votes);
		o.put("voteState", o, comment.voteState);

		o.put("canComment", o, comment.canComment);
		o.put("hasNextPage", o, comment.hasNextPage);

		return o;
	}

	@JSGetter("customData")
	public NativeObject getCustomData() {
		return customData;
	}
}