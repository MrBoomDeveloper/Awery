package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.fromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.intFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.isNullJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogComment;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

public class JsComment extends CatalogComment {

	protected JsComment(@NonNull NativeObject o) {
		if(o.has("items", o)) {
			for(var arrayItem : (NativeArray) o.get("items")) {
				var jsItem = (NativeObject) arrayItem;
				items.add(new JsComment(jsItem));
			}
		}

		authorName = stringFromJs(o.get("authorName"));
		authorAvatar = stringFromJs(o.get("authorAvatar"));
		text = stringFromJs(o.get("text"));
		date = stringFromJs(o.get("date"));

		likes = !isNullJs(o.get("likes")) ? intFromJs(o.get("likes")) : CatalogComment.DISABLED;
		dislikes = !isNullJs(o.get("dislikes")) ? intFromJs(o.get("dislikes")) : CatalogComment.DISABLED;
		comments = !isNullJs(o.get("comments")) ? intFromJs(o.get("comments")) : CatalogComment.DISABLED;
		votes = !isNullJs(o.get("votes")) ? fromJs(o.get("votes"), Integer.class) : null;
		voteState = intFromJs(o.get("voteState"));
		id = stringFromJs(o.get("id"));

		canComment = booleanFromJs(o.get("canComment"));
		hasNextPage = booleanFromJs(o.get("hasNextPage"));

		isEditable = booleanFromJs(o.get("isEditable"));
		isDeletable = booleanFromJs(o.get("isDeletable"));
	}

	public static Scriptable createJsComment(Context context, Scriptable scope, CatalogComment comment) {
		if(comment == null) return null;
		var o = context.newObject(scope);

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

		o.put("isEditable", o, comment.isEditable);
		o.put("isDeletable", o, comment.isDeletable);

		return o;
	}
}