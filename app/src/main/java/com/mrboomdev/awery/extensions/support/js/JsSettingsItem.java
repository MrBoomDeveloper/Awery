package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.intFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.listFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;

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
	public static SettingsItem fromJs(NativeObject o) {
		return new SettingsItem(new SettingsItem() {
			@Override
			public String getTitle(android.content.Context context) {
				return stringFromJs(o.get("title", o));
			}

			@Override
			public String getDescription(android.content.Context context) {
				return stringFromJs(o.get("description", o));
			}

			@Override
			public SettingsItemType getType() {
				return SettingsItemType.valueOf(Objects.requireNonNull(
						stringFromJs(o.get("type", o))).toUpperCase(Locale.ROOT));
			}

			@Override
			public boolean isRestartRequired() {
				return booleanFromJs(o.get("restartRequired", o));
			}

			@Override
			public Float getFrom() {
				return floatFromJs(o.get("from", o));
			}

			@Override
			public Float getTo() {
				return floatFromJs(o.get("to", o));
			}

			@Override
			public Boolean getBooleanValue() {
				return booleanFromJs(o.get("value", o));
			}

			@Override
			public String getStringValue() {
				return stringFromJs(o.get("value", o));
			}

			@Override
			public Integer getIntegerValue() {
				return intFromJs(o.get("value", o));
			}

			@Override
			public List<? extends SettingsItem> getItems() {
				return stream(listFromJs(o.get("items", o), NativeObject.class))
						.filter(Objects::nonNull)
						.map(JsSettingsItem::fromJs)
						.toList();
			}

			@Override
			public String getKey() {
				return stringFromJs(o.get("key", o));
			}

			@Override
			public Set<String> getStringSetValue() {
				return stream(listFromJs(o.get("items", o), Scriptable.class))
						.filter(Objects::nonNull)
						.map(JsBridge::stringFromJs)
						.collect(Collectors.toSet());
			}
		});
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