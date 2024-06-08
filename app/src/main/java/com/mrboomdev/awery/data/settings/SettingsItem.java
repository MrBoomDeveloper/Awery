package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.color.DynamicColors;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryPlatform;
import com.mrboomdev.awery.sdk.util.exceptions.InvalidSyntaxException;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;
import com.squareup.moshi.Json;
import com.squareup.moshi.ToJson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SettingsItem {
	private static final String VAR_PREFIX = "${VAR.";
	public static final SettingsItem INVALID_SETTING = new Builder(SettingsItemType.BOOLEAN)
			.setTitle("Invalid!")
			.setBooleanValue(false)
			.build();

	public static final String SEPARATOR = "_";
	private String key, title, description, icon, behaviour;
	protected SettingsItemType type;
	private String parentKey;
	@Json(name = "tint_icon")
	private Boolean tintIcon;
	@Json(name = "show_if")
	private String showIf;
	private boolean restart;
	private List<SettingsItem> items;
	@Json(name = "header_items")
	private List<SettingsItem> headerItems;
	@Json(ignore = true)
	private SettingsItem parent;
	@Json(name = "icon_size")
	private Float iconSize;
	@Json(name = "boolean_value")
	private Boolean booleanValue;
	@Json(name = "int_value")
	private Integer intValue;
	@Json(name = "string_set_value")
	private Set<String> stringSetValue;
	private Float from, to;
	@Json(name = "string_value")
	private String stringValue;
	@Json(ignore = true)
	private Drawable iconDrawable;

	public SettingsItem(@NonNull SettingsItem item) {
		copyFrom(item);
	}

	public SettingsItem() {}

	public SettingsItem(SettingsItemType type) {
		this.type = type;
	}

	protected void copyFrom(@NonNull SettingsItem item) {
		this.key = item.key;
		this.type = item.type;
		this.items = item.items;

		this.booleanValue = item.booleanValue;
		this.stringValue = item.stringValue;

		this.intValue = item.intValue;
		this.from = item.from;
		this.to = item.to;

		this.behaviour = item.behaviour;
		this.restart = item.restart;
		this.showIf = item.showIf;

		this.icon = item.icon;
		this.iconDrawable = item.iconDrawable;
		this.tintIcon = item.tintIcon;
		this.iconSize = item.iconSize;

		this.title = item.title;
		this.description = item.description;

		this.parentKey = item.parentKey;
		this.parent = item.parent;
	}

	public void setAsParentForChildren() {
		if(items == null) return;

		for(var item : items) {
			item.setParent(this);
			item.setAsParentForChildren();
		}
	}

	public boolean isFromToAvailable() {
		return from != null && to != null;
	}

	public Float getFrom() {
		return from;
	}

	public Float getTo() {
		return to;
	}

	public boolean isDraggable() {
		return false;
	}

	public boolean isDraggableInto(SettingsItem item) {
		return false;
	}

	public float getIconSize() {
		return iconSize == null ? 1 : iconSize;
	}

	public boolean tintIcon() {
		return tintIcon == null || tintIcon;
	}

	public boolean isVisible() {
		if(showIf != null) {
			var requirements = showIf.split(",");

			for(var requirement : requirements) {
				if(!AweryPlatform.getInstance().isRequirementMet(requirement)) return false;
			}
		}

		return true;
	}

	public void setItems(Collection<SettingsItem> items) {
		this.items = List.copyOf(items);
	}

	public String getBehaviour() {
		return behaviour;
	}

	public Drawable getIcon(@NonNull Context context) {
		if(iconDrawable != null) return iconDrawable;
		if(icon == null) return null;

		try {
			var clazz = R.drawable.class;
			var field = clazz.getField(icon);
			var id = field.getInt(null);

			return AppCompatResources.getDrawable(context, id);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	public void restoreValues(AwerySettings settings) {
		if(type == null) return;

		switch(type) {
			case BOOLEAN -> booleanValue = settings.getBoolean(getFullKey(),
					Objects.requireNonNullElse(booleanValue, false));

			case INT, SELECT_INT -> intValue = settings.getInt(getFullKey(),
					Objects.requireNonNullElse(intValue, 0));

			case SELECT, STRING -> stringValue = settings.getString(getFullKey(), stringValue);

			case SCREEN -> {
				if(items == null) return;

				for(var item : items) {
					item.restoreValues(settings);
				}
			}
		}
	}

	public void restoreValues() {
		restoreValues(AwerySettings.getInstance());
	}

	public boolean isRestartRequired() {
		return restart;
	}

	public String getTitle(Context context) {
		var got = getString(context, title);
		if(got != null) return got;
		if(title != null) return title;

		return getKey();
	}

	public String getDescription(Context context) {
		if(description == null) return null;

		var startIndex = description.indexOf(VAR_PREFIX);

		if(startIndex != -1) {
			var endIndex = description.indexOf('}', startIndex + 1);

			if(endIndex == -1) {
				throw new InvalidSyntaxException("No closing '}' found in description!");
			}

			var parsedKey = description.substring(
					startIndex + VAR_PREFIX.length(), endIndex);

			return description.substring(0, startIndex)
					+ SettingsData.resolveValue(parsedKey)
					+ description.substring(endIndex + 1);
		}

		var got = getString(context, description);
		if(got != null) return got;

		return description;
	}

	@Nullable
	private String getString(@NonNull Context context, String name) {
		if(name == null) return null;

		try {
			var clazz = R.string.class;
			var field = clazz.getField(name);
			return context.getString(field.getInt(null));
		} catch(NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	public Boolean getBooleanValue() {
		return booleanValue != null && booleanValue;
	}

	public List<SettingsItem> getHeaderItems() {
		return headerItems;
	}

	public List<SettingsItem> getActionItems() {
		return null;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(int value) {
		intValue = value;
	}

	public String getStringValue() {
		return stringValue;
	}

	public Set<String> getStringSetValue() {
		return stringSetValue;
	}

	public void onClick(Context context) {}

	public boolean onDragged(int fromPosition, int toPosition) {
		return false;
	}

	public void setStringValue(String value) {
		stringValue = value;
	}

	public void setBooleanValue(boolean value) {
		booleanValue = value;
	}

	public void setParent(SettingsItem parent) {
		this.parent = parent;
	}

	public SettingsItem getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public String getFullKey() {
		if(!hasParent()) {
			if(parentKey != null) {
				return parentKey + "_" + key;
			}

			return key;
		}

		return parent.getFullKey() + SEPARATOR + key;
	}

	public String getKey() {
		return key;
	}

	public SettingsItemType getType() {
		return type;
	}

	public List<SettingsItem> getItems() {
		return items;
	}

	private SettingsItem findDirect(String key) {
		return stream(items)
				.filter(item -> item.getKey().equals(key))
				.findFirst()
				.orElse(null);
	}

	public SettingsItem find(@NonNull String query) {
		return switch(type) {
			case BOOLEAN, INT, STRING, COLOR, SELECT, SELECT_INT, MULTISELECT ->
					query.equals(getFullKey()) ? this : null;

			case SCREEN, SCREEN_BOOLEAN -> {
				if(query.equals(getFullKey())) {
					yield this;
				}

				if(items == null) {
					yield null;
				}

				for(var item : items) {
					var found = item.find(query);

					if(found != null) {
						yield found;
					}
				}

				yield null;
			}

			case ACTION, DIVIDER, CATEGORY -> null;
		};
	}

	public static class Builder {
		private final SettingsItem item = new SettingsItem();

		public Builder(SettingsItemType type) {
			item.type = type;
		}

		public Builder setKey(String key) {
			item.key = key;
			return this;
		}

		public Builder setBooleanValue(boolean value) {
			item.booleanValue = value;
			return this;
		}

		public Builder setTitle(String title) {
			item.title = title;
			return this;
		}

		public Builder setDescription(String description) {
			item.description = description;
			return this;
		}

		public Builder setIcon(String icon) {
			item.icon = icon;
			return this;
		}

		public Builder setStringSetValue(Set<String> values) {
			item.stringSetValue = values;
			return this;
		}

		public Builder setRestartRequired(boolean restart) {
			item.restart = restart;
			return this;
		}

		public Builder setBehaviour(String behaviour) {
			item.behaviour = behaviour;
			return this;
		}

		public Builder setIcon(Drawable drawable) {
			item.iconDrawable = drawable;
			return this;
		}

		public Builder setIconSize(float size) {
			item.iconSize = size;
			return this;
		}

		public Builder setTintIcon(boolean tint) {
			item.tintIcon = tint;
			return this;
		}

		public Builder setItems(Collection<? extends SettingsItem> items) {
			item.items = new ArrayList<>(items);
			return this;
		}

		public Builder setParent(SettingsItem parent) {
			item.parent = parent;
			return this;
		}

		public SettingsItem build() {
			return item;
		}
	}

	@SuppressWarnings("unused")
	public static class Adapter {

		@ToJson
		public SettingsItem toJson(SettingsItem item) {
			var newItem = new SettingsItem(item);

			if(item.getParent() != null) {
				newItem.parentKey = item.getParent().getFullKey();
			}

			return newItem;
		}
	}
}