package com.mrboomdev.awery.ext.data;

import androidx.annotation.NonNull;

import com.squareup.moshi.Json;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class Setting {
	private static final WeakHashMap<Setting, Set<Observer>> observers = new WeakHashMap<>();
	private Settings items, actions, headerItems;
	private Image icon;
	private String title, description, key, extra;
	private Float from, to;
	private Type type;
	@Json(ignore = true)
	private Setting parent;
	private Object value;

	public Setting(Type type) {
		this.type = type;
	}

	public Setting() {}

	public void setParent(Setting setting) {
		this.parent = setting;
	}

	@Nullable
	public Float getFrom() {
		return from;
	}

	@Nullable
	public Float getTo() {
		return to;
	}

	public void setAsParentForChildren() {
		if(getItems() != null) {
			for(var item : getItems()) {
				item.setParent(this);
				item.setAsParentForChildren();
			}
		}
	}

	@Nullable
	public Setting find(@NonNull String key) {
		if(key.equals(getKey())) {
			return this;
		}

		if(getItems() != null) {
			for(var item : getItems()) {
				var found = item.find(key);
				if(found != null) return found;
			}
		}

		return null;
	}

	@Nullable
	public String getExtra() {
		return extra;
	}

	@Nullable
	public Setting getParent() {
		return parent;
	}

	public boolean isDraggable() {
		return false;
	}

	public boolean isDraggableInto(Setting into) {
		return false;
	}

	public boolean onDragged(int from, int to) {
		return false;
	}

	@Nullable
	public Image getIcon() {
		return icon;
	}

	@Nullable
	public Settings getItems() {
		return items;
	}

	@Nullable
	public Settings getActions() {
		return actions;
	}

	@Nullable
	public Settings getHeaderItems() {
		return headerItems;
	}

	@Nullable
	public String getKey() {
		return key;
	}

	public void onClick() {}

	@Nullable
	public String getTitle() {
		return title;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	/**
	 * Possible value types:
	 * {@link String}, {@link Integer}, {@link Boolean}, {@link Long}, {@link Selection}, {@link Selection.State}, {@link Set<Setting>}
	 */
	@Nullable
	public Object getValue() {
		return value;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public final Set<Setting> getSetValue() {
		return (Set<Setting>) getValue();
	}

	@Nullable
	public final String getStringValue() {
		return (String) getValue();
	}

	@Nullable
	public final Boolean getBooleanValue() {
		return (Boolean) getValue();
	}

	@Nullable
	public final Integer getIntegerValue() {
		return (Integer) getValue();
	}

	@Nullable
	public final Selection.State getExcludableValue() {
		return (Selection.State) getValue();
	}

	/**
	 * Possible value types:
	 * {@link String}, {@link Integer}, {@link Boolean}, {@link Long}, {@link Selection}, {@link Selection.State}, {@link Set<Setting>}
	 */
	public void setValue(Object o) {
		if(getType() != null) {
			getType().checkIfValidValue(o);
		}

		this.value = o;
	}

	public final void setValue(Set<Setting> set) {
		setValue((Object) set);
	}

	public final void setValue(String string) {
		setValue((Object) string);
	}

	public final void setValue(Integer integer) {
		setValue((Object) integer);
	}

	public final void setValue(Boolean bool) {
		setValue((Object) bool);
	}

	public final void setValue(Selection<?> selection) {
		setValue((Object) selection);
	}

	public final void setValue(Long longValue) {
		setValue((Object) longValue);
	}

	public final void setValue(Selection.State excludable) {
		setValue((Object) excludable);
	}

	@Nullable
	public Type getType() {
		return type;
	}

	private static void invokeObservers(Setting parent, Consumer<Observer> consumer) {
		var set = observers.get(parent);
		if(set == null) return;

		for(var observer : set) {
			consumer.accept(observer);
		}
	}

	public void addSettingsObserver(Observer observer) {
		observers.computeIfAbsent(this, k -> new HashSet<>()).add(observer);
	}

	public void removeSettingsObserver(Observer observer) {
		observers.computeIfAbsent(this, k -> new HashSet<>()).remove(observer);
	}

	public void onSettingAddition(Setting item, int position) {
		invokeObservers(this, observer -> observer.onSettingAddition(item, position));
	}

	public void onSettingRemoval(Setting item) {
		invokeObservers(this, observer -> observer.onSettingRemoval(item));
	}

	public void onSettingChange(Setting newItem, Setting oldItem) {
		invokeObservers(this, observer -> observer.onSettingChange(newItem, oldItem));
	}

	public void onSettingChange(Setting item) {
		onSettingChange(item, item);
	}

	public interface Observer {
		default void onSettingAddition(Setting item, int position) {}
		default void onSettingRemoval(Setting item) {}
		default void onSettingChange(Setting newItem, Setting oldItem) {}
	}

	public enum Type {
		@Json(name = "boolean")
		BOOLEAN {
			@Override
			public boolean isBoolean() {
				return true;
			}
		},

		@Json(name = "screen_boolean")
		SCREEN_BOOLEAN {
			@Override
			public boolean isBoolean() {
				return true;
			}
		},

		@Json(name = "serializable")
		SERIALIZABLE,
		@Json(name = "date")
		DATE,
		@Json(name = "excludable")
		EXCLUDABLE,
		@Json(name = "divider")
		DIVIDER,
		@Json(name = "category")
		CATEGORY,
		@Json(name = "color")
		COLOR,
		@Json(name = "integer")
		INTEGER,
		@Json(name = "string")
		STRING,
		@Json(name = "screen")
		SCREEN,
		@Json(name = "select")
		SELECT,
		@Json(name = "select_integer")
		SELECT_INTEGER,
		@Json(name = "multiselect")
		MULTISELECT,
		@Json(name = "action")
		ACTION;

		public void checkIfValidValue(Object o) {
			if(!switch(this) {
				case STRING -> o instanceof String;
				case INTEGER, SELECT_INTEGER, COLOR -> o instanceof Integer;
				case BOOLEAN, SCREEN_BOOLEAN -> o instanceof Boolean;
				case SERIALIZABLE -> o instanceof Serializable;

				case EXCLUDABLE -> {
					if(!(o instanceof Selection.State)) {
						throw new IllegalArgumentException("Setting type is \"" + this
								+ "\", but the value is type of \""
								+ o.getClass().getSimpleName() + "\". Required value type is Selection.State");
					}

					yield true;
				}

				case DATE -> {
					if(!(o instanceof Long)) {
						throw new IllegalArgumentException("Setting type is \"" + this
								+ "\", but the value is type of \""
								+ o.getClass().getSimpleName() + "\". Required value type is Long");
					}

					yield true;
				}

				case SELECT -> {
					if(!(o instanceof Setting)) {
						throw new IllegalArgumentException("Setting type is \"" + this
								+ "\", but the value is type of \""
								+ o.getClass().getSimpleName() + "\". Required value type is Setting");
					}

					yield true;
				}

				case MULTISELECT -> {
					if(!(o instanceof Set<?> set)) {
						throw new IllegalArgumentException("Setting type is \"" + this
								+ "\", but the value is type of \""
								+ o.getClass().getSimpleName() + "\". Required value type is Set<Setting>");
					}

					for(var item : set) {
						if(!(item instanceof Setting)) {
							throw new IllegalArgumentException("One of items was not of type Setting!");
						}
					}

					yield true;
				}

				default -> throw new UnsupportedOperationException(
						"\"" + this + "\" doesn't support any values!");
			}) {
				throw new IllegalArgumentException("Setting type is \"" + this
						+ "\", but the value is type of \"" + o.getClass().getSimpleName() + "\"");
			}
		}

		public boolean isBoolean() {
			return false;
		}
	}

	public static class Builder {
		private final Setting setting = new Setting();
		private Object pendingValue;

		public Builder(Type type) {
			setting.type = type;
		}

		public Builder() {}

		public Builder setTitle(String title) {
			setting.title = title;
			return this;
		}

		public Builder setIcon(Image icon) {
			setting.icon = icon;
			return this;
		}

		public Builder setFrom(Float from) {
			setting.from = from;
			return this;
		}

		public Builder setTo(Float to) {
			setting.to = to;
			return this;
		}

		public Builder setFromTo(Float from, Float to) {
			setting.from = from;
			setting.to = to;
			return this;
		}

		public Builder setValue(Object o) {
			pendingValue = o;
			return this;
		}

		public Builder setExtra(String extra) {
			setting.extra = extra;
			return this;
		}

		public Builder setItems(Settings items) {
			setting.items = items;
			return this;
		}

		public Builder setItems(Setting... items) {
			return setItems(new Settings(items));
		}

		public Builder setActions(Settings items) {
			setting.actions = items;
			return this;
		}

		public Builder setHeaderItems(Settings items) {
			setting.headerItems = items;
			return this;
		}

		public Builder setDescription(String description) {
			setting.description = description;
			return this;
		}

		public Builder setKey(String key) {
			setting.key = key;
			return this;
		}

		public Setting build() {
			setting.setValue(pendingValue);
			return setting;
		}
	}
}