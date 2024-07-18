package com.mrboomdev.awery.data.settings;

import static com.mrboomdev.awery.app.AweryApp.getString;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryPlatform;
import com.mrboomdev.awery.sdk.util.exceptions.InvalidSyntaxException;
import com.mrboomdev.awery.ui.activity.settings.SettingsActions;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.Selection;
import com.squareup.moshi.Json;
import com.squareup.moshi.ToJson;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SettingsItem implements Serializable {
	private static final String VAR_PREFIX = "${VAR.";
	private String key, title, description, icon, behaviour;
	protected SettingsItemType type;
	private String parentKey;
	@Json(name = "tint_icon")
	private Boolean tintIcon;
	@Json(name = "show_if")
	protected String showIf;
	protected boolean restart;
	private List<? extends SettingsItem> items;
	@Json(name = "header_items")
	protected List<SettingsItem> headerItems;
	@Json(ignore = true)
	protected SettingsItem parent;
	@Json(name = "icon_size")
	protected Float iconSize;
	@Json(name = "boolean_value")
	protected Boolean booleanValue;
	@Json(name = "integer_value")
	protected Integer integerValue;
	@Json(name = "excludable_value")
	protected Selection.State excludableValue;
	@Json(name = "long_value")
	protected Long longValue;
	@Json(name = "string_set_value")
	protected Set<String> stringSetValue;
	protected Float from, to;
	@Json(name = "string_value")
	protected String stringValue;
	@Json(ignore = true)
	private transient Drawable iconDrawable;

	public SettingsItem(@NonNull SettingsItem item) {
		copyFrom(item);
	}

	public SettingsItem() {}

	public SettingsItem(SettingsItemType type) {
		this.type = type;
	}

	public SettingsItem(SettingsItemType type, String key) {
		this.type = type;
		this.key = key;
	}

	public SettingsItem(SettingsItemType type, String key, int intValue) {
		this.type = type;
		this.key = key;
		this.integerValue = intValue;
	}

	public SettingsItem(SettingsItemType type, String key, String stringValue) {
		this.type = type;
		this.key = key;
		this.stringValue = stringValue;
	}

	protected void copyFrom(@NonNull SettingsItem item) {
		this.key = item.getKey();
		this.type = item.getType();
		this.items = item.getItems();

		this.booleanValue = item.getBooleanValue();
		this.stringValue = item.getStringValue();
		this.integerValue = item.getIntegerValue();
		this.longValue = item.getLongValue();
		this.excludableValue = item.getExcludableValue();

		this.from = item.getFrom();
		this.to = item.getTo();
		this.behaviour = item.getBehaviour();
		this.restart = item.isRestartRequired();
		this.showIf = item.showIf;

		this.icon = item.getRawIcon();
		this.iconDrawable = item.iconDrawable;
		this.tintIcon = item.tintIcon;
		this.iconSize = item.iconSize;

		this.title = item.title != null ? item.title
				: item.getTitle(getAnyContext());

		this.description = item.description != null ? item.description
				: item.getDescription(getAnyContext());

		this.parentKey = item.parentKey;
		this.parent = item.parent;
	}

	public String getRawIcon() {
		return icon;
	}

	public void setAsParentForChildren() {
		if(items == null) return;

		for(var item : items) {
			item.setParent(this);
			item.setAsParentForChildren();
		}
	}

	public boolean isFromToAvailable() {
		return getFrom() != null && getTo() != null;
	}

	public Float getFrom() {
		return from;
	}

	public Selection.State getExcludableValue() {
		return excludableValue;
	}

	public void setExcludableValue(Selection.State state) {
		this.excludableValue = state;
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

	public Long getLongValue() {
		return longValue;
	}

	public void setValue(Long value) {
		this.longValue = value;
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

	public String getBehaviour() {
		return behaviour;
	}

	public Drawable getIcon(@NonNull Context context) {
		if(iconDrawable != null) return iconDrawable;
		if(getRawIcon() == null) return null;

		try {
			var clazz = R.drawable.class;
			var field = clazz.getField(getRawIcon());
			var id = field.getInt(null);

			return AppCompatResources.getDrawable(context, id);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	public void restoreSavedValues(SettingsDataHandler handler) {
		if(getItems() != null) {
			for(var item : getItems()) {
				item.restoreSavedValues(handler);
			}
		}

		if(getType() == null) return;

		switch(getType()) {
			case BOOLEAN, SCREEN_BOOLEAN -> setValue((Boolean) handler.restoreValue(this));
			case INTEGER, SELECT_INTEGER, COLOR -> setValue((Integer) handler.restoreValue(this));
			case SELECT, STRING, JSON -> setValue((String) handler.restoreValue(this));
			case DATE -> setValue((Long) handler.restoreValue(this));
		}
	}
	
	public <T> T parseJsonValue(Class<T> clazz) throws IOException {
		return Parser.fromString(clazz, getStringValue());
	}

	public <T> T parseJsonValue(Parser.Adapter<T> adapter) throws IOException {
		return Parser.fromString(adapter, getStringValue());
	}

	/**
	 * Call before showing any data to a user
	 * @author MrBoomDev
	 */
	public void restoreSavedValues() {
		restoreSavedValues(getPrefs());
	}

	public boolean isRestartRequired() {
		return restart;
	}

	public String getTitle(Context context) {
		var got = getString(R.string.class, title);
		return got != null ? got : title;
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

		var got = getString(R.string.class, description);
		return got != null ? got : description;
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

	public Integer getIntegerValue() {
		return integerValue;
	}

	public void setValue(Integer value) {
		integerValue = value;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setValue(Selection.State state) {
		excludableValue = state;
	}

	public Set<String> getStringSetValue() {
		return stringSetValue;
	}

	public void onClick(Context context) {
		SettingsActions.run(this);
	}

	public boolean onDragged(int fromPosition, int toPosition) {
		return false;
	}

	public void setValue(String value) {
		stringValue = value;
	}

	public void setValue(Set<String> set) {
		this.stringSetValue = set;
	}

	public void clearValue() {
		stringValue = null;
		integerValue = null;
		booleanValue = null;
		stringSetValue = null;
		longValue = null;
	}

	public void setValue(Boolean value) {
		booleanValue = value;
	}

	public void setParent(SettingsItem parent) {
		this.parent = parent;
	}

	public SettingsItem getParent() {
		return parent;
	}

	public String getKey() {
		return key;
	}

	public SettingsItemType getType() {
		return type;
	}

	public List<? extends SettingsItem> getItems() {
		return items;
	}

	public SettingsItem findItem(@NonNull String query) {
		if(query.equals(getKey())) {
			return this;
		}

		if(getItems() != null) {
			for(var item : getItems()) {
				var found = item.findItem(query);

				if(found != null) {
					return found;
				}
			}
		}

		return null;
	}

	public static class Builder {
		private final SettingsItem item = new SettingsItem();

		public Builder(SettingsItemType type) {
			item.type = type;
		}

		public Builder() {}

		public Builder setKey(String key) {
			item.key = key;
			return this;
		}
		
		public Builder setValue(Integer value) {
			item.integerValue = value;
			return this;
		}

		public Builder setValue(Long value) {
			item.longValue = value;
			return this;
		}

		public Builder setValue(Boolean value) {
			item.booleanValue = value;
			return this;
		}

		public Builder setValue(String value) {
			item.stringValue = value;
			return this;
		}

		public Builder setTitle(String title) {
			item.title = title;
			return this;
		}

		public Builder setValue(Selection.State state) {
			item.excludableValue = state;
			return this;
		}

		public Builder setTitle(@StringRes int title) {
			item.title = getAnyContext().getString(title);
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
		
		public Builder setIcon(@DrawableRes int res) {
			return setIcon(getAnyContext().getResources().getResourceEntryName(res));
		}

		public Builder setValue(Set<String> values) {
			item.stringSetValue = values;
			return this;
		}

		public Builder setRestartRequired(boolean restart) {
			item.restart = restart;
			return this;
		}

		public Builder setFrom(Float from) {
			item.from = from;
			return this;
		}

		public Builder setTo(Float to) {
			item.to = to;
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
			item.items = items == null ? null : new ArrayList<>(items);
			return this;
		}

		public Builder setParent(SettingsItem parent) {
			item.parent = parent;
			return this;
		}

		public SettingsItem build() {
			return item;
		}

		public CustomSettingsItem buildCustom() {
			return new CustomSettingsItem(item) {};
		}
	}

	@SuppressWarnings("unused")
	public static class Adapter {

		@ToJson
		public SettingsItem toJson(SettingsItem item) {
			var newItem = new SettingsItem(item);

			if(item.getParent() != null) {
				newItem.parentKey = item.getParent().getKey();
			}

			return newItem;
		}
	}
}