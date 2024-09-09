package com.mrboomdev.awery.extensions.support.aweryjs;

import static com.mrboomdev.awery.util.NiceUtils.asFloat;
import static com.mrboomdev.awery.util.NiceUtils.returnIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueBoolean;
import com.caoccao.javet.values.primitive.V8ValueNumber;
import com.caoccao.javet.values.primitive.V8ValuePrimitive;
import com.caoccao.javet.values.primitive.V8ValueString;
import com.caoccao.javet.values.reference.IV8ValueArray;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueError;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.mrboomdev.awery.app.data.settings.base.SettingsItem;
import com.mrboomdev.awery.app.data.settings.base.SettingsItemType;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.util.exceptions.JsException;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import java9.util.Sets;

public class AweryJsConverter {

	@NonNull
	@Contract("_ -> new")
	@SuppressWarnings("unchecked")
	public static SettingsList toSettingsList(V8ValueArray array) throws JavetException {
		if(array == null || array.isNullOrUndefined()) {
			return new SettingsList();
		}

		return new SettingsList(stream(toJavaIterable(array))
				.map(item -> {
					if(!(item instanceof V8ValueObject o)) {
						throw new IllegalStateException("Settings item isn't typeof Object!");
					}

					try {
						var type = returnIfNotNull(o.getString("type"), SettingsItemType::valueOf);
						var value = o.get("value");

						var builder = new SettingsItem.Builder(type)
								.setKey(o.getString("key"))
								.setTitle(o.getString("title"))
								.setDescription(o.getString("description"))
								.setFrom(asFloat(o.getString("from")))
								.setTo(asFloat(o.getString("to")))
								.setItems(o.get("items") instanceof V8ValueArray nestedArray ? toSettingsList(nestedArray) : null);

						if(value != null) {
							if(value instanceof V8ValueString s) {
								builder.setValue(s.getValue());
							} else if(value instanceof V8ValueNumber<?> number) {
								if(type != null) {
									switch(type) {
										case INTEGER, SELECT_INTEGER, COLOR -> builder.setValue(number.asInt());
										case DATE -> builder.setValue(number.asLong());
									}
								} else {
									builder.setValue(number.asInt());
									builder.setValue(number.asLong());
								}
							} else if(value instanceof V8ValueBoolean bool) {
								builder.setValue(bool.getValue());
							} else if(value instanceof List<?> list) {
								builder.setValue((Set<String>) Sets.copyOf(list));
							} else {
								throw new IllegalArgumentException("Unknown value type! " + value.getClass().getName());
							}
						}

						return builder.buildCustom();
					} catch(JavetException e) {
						throw new RuntimeException(e);
					}
				})
				.toList());
	}

	@NonNull
	public static Map<V8Value, V8Value> toJavaMap(@NonNull V8ValueObject o) throws JavetException {
		var map = new HashMap<V8Value, V8Value>();

		for(var key : toJavaIterable(o.getPropertyNames())) {
			map.put(key, o.get(key));
		}

		return map;
	}

	@NonNull
	public static List<V8Value> toJavaList(V8ValueArray array) throws JavetException {
		var iterator = toJavaIterable(array).iterator();
		var list = new ArrayList<V8Value>(array.getLength());

		while(iterator.hasNext()) {
			list.add(iterator.next());
		}

		return list;
	}

	@NonNull
	public static <T extends V8Value> Iterable<T> toJavaIterable(@NonNull IV8ValueArray array) throws JavetException {
		var length = array.getLength();

		return new Iterable<>() {
			@NonNull
			@Override
			public Iterator<T> iterator() {
				return new Iterator<>() {
					private int cursor;

					@Override
					public boolean hasNext() {
						return cursor < length;
					}

					@Override
					public T next() {
						int i = cursor;

						if(i >= length) {
							throw new NoSuchElementException();
						}

						cursor = i + 1;

						try {
							return array.get(i);
						} catch(JavetException e) {
							throw new RuntimeException(e);
						}
					}
				};
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T> T toJava(V8Value value) throws JavetException {
		if(value == null || value.isNullOrUndefined()) {
			return null;
		}

		if(value instanceof V8ValuePrimitive<?> primitive) {
			return (T) primitive.getValue();
		}

		if(value instanceof V8ValueError error) {
			return (T) new JsException(error);
		}

		if(value instanceof V8ValueArray array) {
			return (T) toJavaList(array);
		}

		if(value instanceof V8ValueObject o) {
			return (T) toJavaMap(o);
		}

		throw new UnsupportedOperationException("Unsupported V8Value type! " + value.getClass().getName());
	}
}