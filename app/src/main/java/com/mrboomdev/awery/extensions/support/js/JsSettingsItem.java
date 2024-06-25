package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.intFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.listFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.longFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.returnIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Selection;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import java9.util.stream.Collectors;

public class JsSettingsItem extends SettingsItem {

	private JsSettingsItem() {}

	@NonNull
	@Contract(value = "_ -> new", pure = true)
	public static SettingsItem fromJs(@NonNull NativeObject o) {
		var type = SettingsItemType.valueOf(Objects.requireNonNull(
				stringFromJs(o.get("type", o))).toUpperCase(Locale.ROOT));

		return new SettingsItem.Builder(type)
				.setTitle(stringFromJs(o.get("title", o)))
				.setDescription(stringFromJs(o.get("description", o)))
				.setRestartRequired(booleanFromJs(o.get("restartRequired", o)))
				.setFrom(floatFromJs(o.get("from", o)))
				.setTo(floatFromJs(o.get("to", o)))
				.setValue(booleanFromJs(o.get("value", o)))
				.setValue(intFromJs(o.get("value", o)))
				.setValue(stringFromJs(o.get("value", o)))
				.setValue(longFromJs(o.get("value", o)))
				.setKey(stringFromJs(o.get("key", o)))
				.setValue(NiceUtils.<Set<String>, List<Scriptable>>returnWith(listFromJs(o.get("items", o), Scriptable.class), list -> {
					if(list == null) return null;

					return stream(list)
							.filter(Objects::nonNull)
							.map(JsBridge::stringFromJs)
							.collect(Collectors.toSet());
				}))
				.setItems(NiceUtils.returnWith(listFromJs(o.get("items", o), NativeObject.class), items -> {
					if(items == null) return null;

					return stream(listFromJs(o.get("items", o), NativeObject.class))
							.filter(Objects::nonNull)
							.map(JsSettingsItem::fromJs)
							.toList();
				}))
				.setValue(NiceUtils.<Selection.State, String>returnWith(stringFromJs(o.get("value", o)), string -> {
					if(string == null) return null;

					return switch(string) {
						case "excluded" -> Selection.State.EXCLUDED;
						case "selected" -> Selection.State.SELECTED;
						case "unselected" -> Selection.State.UNSELECTED;
						default -> null;
					};
				}))
				.buildCustom();
	}

	@NonNull
	public static Scriptable toJs(@NonNull SettingsItem setting, @NonNull Context context, Scriptable scope) {
		var o = context.newObject(scope);
		var ctx = getAnyContext();

		o.put("key", o, setting.getKey());
		o.put("title", o, setting.getTitle(ctx));
		o.put("description", o, setting.getDescription(ctx));
		o.put("from", o, setting.getFrom());
		o.put("to", o, setting.getTo());
		o.put("restartRequired", o, setting.isRestartRequired());

		switch(setting.getType()) {
			case SCREEN, SELECT, SELECT_INTEGER, MULTISELECT, SCREEN_BOOLEAN -> o.put("items", o,
					stream(setting.getItems()).map(item -> toJs(item, context, o)));
		}

		o.put("value", o, switch(setting.getType()) {
			case EXCLUDABLE -> Objects.requireNonNullElse(returnIfNotNull(
					setting.getExcludableValue(), state -> switch(state) {
				case EXCLUDED -> "excluded";
				case SELECTED -> "selected";
				case UNSELECTED -> "unselected";
			}), Selection.State.UNSELECTED);

			case BOOLEAN, SCREEN_BOOLEAN -> setting.getBooleanValue();
			case DATE -> setting.getLongValue();
			case DIVIDER, CATEGORY, COLOR, SCREEN, ACTION -> null;
			case INTEGER, SELECT_INTEGER -> setting.getIntegerValue();
			case STRING, SELECT -> setting.getStringValue();
			case MULTISELECT -> setting.getStringSetValue();
		});

		o.put("type", o, setting.getType()
				.name().toLowerCase(Locale.ROOT));

		return o;
	}
}