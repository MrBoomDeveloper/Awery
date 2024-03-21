package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.AweryApp.stream;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.color.DynamicColors;
import com.mrboomdev.awery.R;
import com.squareup.moshi.Json;
import com.squareup.moshi.ToJson;

import java.util.List;

public class SettingsItem {
	private final String key, title, description, icon, behaviour;
	private final SettingsItemType type;
	private String parentKey;
	@Json(name = "tint_icon")
	private Boolean tintIcon;
	@Json(name = "show_if")
	private String showIf;
	private final boolean restart;
	private List<SettingsItem> items;
	@Json(ignore = true)
	private SettingsItem parent;
	@Json(name = "boolean_value")
	private Boolean booleanValue;
	@Json(name = "int_value")
	private Integer intValue;

	public SettingsItem(@NonNull SettingsItem item) {
		this.key = item.key;
		this.type = item.type;
		this.items = item.items;
		this.restart = item.restart;
		this.behaviour = item.behaviour;

		this.icon = item.icon;
		this.tintIcon = item.tintIcon;
		this.title = item.title;
		this.description = item.description;

		this.showIf = item.showIf;
		this.booleanValue = item.booleanValue;
		this.intValue = item.intValue;

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

	public boolean tintIcon() {
		return tintIcon == null || tintIcon;
	}

	public boolean isVisible() {
		if(showIf != null) {
			var requirements = showIf.split(",");

			for(var requirement : requirements) {
				if(requirement.equals("is_material_you_available")) {
					return DynamicColors.isDynamicColorAvailable();
				}

				if(requirement.equals("never")) {
					return false;
				}
			}
		}

		return true;
	}

	public String getBehaviour() {
		return behaviour;
	}

	public Drawable getIcon(@NonNull Context context) {
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
		switch(type) {
			case BOOLEAN -> booleanValue = settings.getBoolean(getFullKey());

			case INT -> intValue = settings.getInt(getFullKey());

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

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int value) {
		intValue = value;
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

	public void mergeValues(SettingsItem item) {
		if(item == null) return;

		if(item.booleanValue != null) {
			booleanValue = item.booleanValue;
		}

		if(item.intValue != null) {
			intValue = item.intValue;
		}

		if(items != null) {
			for(var child : items) {
				child.mergeValues(item.findDirect(child.getKey()));
			}
		} else {
			items = item.items;
		}
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

		return parent.getFullKey() + "_" + key;
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
		var parts = query.split("\\.");

		if(parts.length == 1) {
			return findDirect(parts[0]);
		}

		if(parts.length > 1) {
			return stream(items)
					.filter(item -> item.getKey().equals(parts[0]))
					.map(item -> item.find(String.join(".", parts).substring(parts[0].length())))
					.findFirst()
					.orElse(null);
		}

		return null;
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