package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.intFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.listFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.longFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.NiceUtils.returnIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Selection;

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
	public static SettingsItem fromJs(@NonNull NativeObject o, @Nullable SettingsItemType parent) {
		var type = (parent == SettingsItemType.SELECT ||
				parent == SettingsItemType.SELECT_INTEGER ||
				parent == SettingsItemType.MULTISELECT)
				? null : SettingsItemType.valueOf(requireArgument(
						o, "type", String.class).toUpperCase(Locale.ROOT));

		var builder = new SettingsItem.Builder(type)
				.setTitle(stringFromJs(o.get("title")))
				.setDescription(stringFromJs(o.get("description")))
				.setRestartRequired(booleanFromJs(o.get("restartRequired")))
				.setFrom(floatFromJs(o.get("from")))
				.setTo(floatFromJs(o.get("to")))
				.setKey(stringFromJs(o.get("key")))
				.setItems(NiceUtils.returnWith(listFromJs(o.get("items"), NativeObject.class), items -> {
					if(items == null) return null;

					return stream(listFromJs(o.get("items"), NativeObject.class))
							.filter(Objects::nonNull)
							.map(item -> JsSettingsItem.fromJs(item, type))
							.toList();
				}));

		if(type != null) {
			switch(type) {
				case BOOLEAN, SCREEN_BOOLEAN -> builder.setValue(booleanFromJs(o.get("value", o)));
				case INTEGER, SELECT_INTEGER -> builder.setValue(intFromJs(o.get("value", o)));
				case STRING, SELECT -> builder.setValue(stringFromJs(o.get("value", o)));
				case DATE -> builder.setValue(longFromJs(o.get("value", o)));

				case EXCLUDABLE -> builder.setValue(
						NiceUtils.<Selection.State, String>returnWith(
								stringFromJs(o.get("value", o)), string -> {
									if(string == null) return null;

									return switch(string) {
										case "EXCLUDED" -> Selection.State.EXCLUDED;
										case "SELECTED" -> Selection.State.SELECTED;
										case "UNSELECTED" -> Selection.State.UNSELECTED;
										default -> null;
									};
								}));

				case MULTISELECT -> builder.setValue(
						NiceUtils.<Set<String>, List<Scriptable>>returnWith(
								listFromJs(o.get("items", o), Scriptable.class), list -> {
									if(list == null) return null;

									return stream(list)
											.filter(Objects::nonNull)
											.map(JsBridge::stringFromJs)
											.collect(Collectors.toSet());
								}));
			}
		}

		return builder.buildCustom();
	}

	@NonNull
	public static Scriptable toJs(@NonNull SettingsItem setting, @NonNull Context context, Scriptable scope) {
		var o = context.newObject(scope);
		var ctx = getAnyContext();
		var type = setting.getType();

		o.put("key", o, setting.getKey());
		o.put("title", o, setting.getTitle(ctx));
		o.put("description", o, setting.getDescription(ctx));
		o.put("from", o, setting.getFrom());
		o.put("to", o, setting.getTo());
		o.put("restartRequired", o, setting.isRestartRequired());

		if(type != null) {
			o.put("type", o, setting.getType()
					.name().toLowerCase(Locale.ROOT));
		}

		if(setting.getItems() != null) {
			o.put("items", o, stream(setting.getItems())
					.map(item -> toJs(item, context, o))
					.toList());
		}

		if(type == null) {
			type = predictType(setting);
		}

		if(type != null) {
			o.put("value", o, switch(type) {
				case EXCLUDABLE -> Objects.requireNonNullElse(returnIfNotNull(
						setting.getExcludableValue(), Enum::name), Selection.State.UNSELECTED);

				case BOOLEAN, SCREEN_BOOLEAN -> setting.getBooleanValue();
				case DATE -> setting.getLongValue();
				case DIVIDER, CATEGORY, COLOR, SCREEN, ACTION -> null;
				case INTEGER, SELECT_INTEGER -> setting.getIntegerValue();
				case STRING, SELECT, JSON, SERIALIZABLE -> setting.getStringValue();
				case MULTISELECT -> setting.getStringSetValue();
			});
		}

		return o;
	}

	@Nullable
	private static SettingsItemType predictType(@NonNull SettingsItem item) {
		if(item.getStringValue() != null) return SettingsItemType.STRING;
		if(item.getBooleanValue() != null) return SettingsItemType.BOOLEAN;
		if(item.getIntegerValue() != null) return SettingsItemType.INTEGER;
		if(item.getExcludableValue() != null) return SettingsItemType.EXCLUDABLE;
		if(item.getStringSetValue() != null) return SettingsItemType.MULTISELECT;
		if(item.getLongValue() != null) return SettingsItemType.DATE;
		return null;
	}
}