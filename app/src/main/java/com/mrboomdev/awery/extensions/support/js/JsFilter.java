package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.intFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Locale;
import java.util.Objects;

public class JsFilter extends CatalogFilter {

	public JsFilter(@NonNull CatalogFilter original) {
		super(original.getType(), original.getId(), original.getTitle(), original.getValue());
	}

	public JsFilter(@NonNull NativeObject o) {
		super(Type.valueOf(Objects.requireNonNull(
						stringFromJs(o.get("type", o))).toUpperCase(Locale.ROOT)),

				stringFromJs(o.get("id", o)),
				stringFromJs(o.get("title", o)),

				switch(Type.valueOf(Objects.requireNonNull(
						stringFromJs(o.get("type", o))).toUpperCase(Locale.ROOT))) {
					case STRING -> stringFromJs(o.get("value", o));
					case FLOAT -> floatFromJs(o.get("value", o));
					case INTEGER -> intFromJs(o.get("value", o));
					case BOOLEAN -> booleanFromJs(o.get("value", o));
					/*case DISABLEABLE -> null;
					case DATE -> null;
					case NESTED_FILTERS -> null;*/
					default -> throw new UnimplementedException("TODO!!!");
				});
	}

	public Scriptable toScriptable(@NonNull Context context, Scriptable scope) {
		var obj = context.newObject(scope);
		obj.put("id", obj, getId());
		obj.put("type", obj, getType().name().toLowerCase(Locale.ROOT));

		obj.put("value", obj, switch(getType()) {
			case STRING, INTEGER, FLOAT, BOOLEAN, DATE -> getValue();

			case NESTED_FILTERS -> stream(getItems())
					.map(filter -> new JsFilter(filter).toScriptable(context, scope))
					.toArray();

			case DISABLEABLE -> switch(getDisablableMode()) {
				case CHECKED -> "checked";
				case UNCHECKED -> "unchecked";
				case DISABLED -> "disabled";
			};
		});

		return obj;
	}
}