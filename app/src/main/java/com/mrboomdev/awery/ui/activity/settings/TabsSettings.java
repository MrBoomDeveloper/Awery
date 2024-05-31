package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;

import java.util.ArrayList;
import java.util.List;

public class TabsSettings extends SettingsItem {
	private final List<SettingsItem> tabs = new ArrayList<>();
	private final List<SettingsItem> headerItems = List.of(
			new SettingsItem(SettingsItemType.ACTION) {
				@Override
				public Drawable getIcon(@NonNull Context context) {
					return ContextCompat.getDrawable(context, R.drawable.ic_add);
				}

				@Override
				public void onClick(Context context) {
					toast("Currently unavailable");
				}
			}
	);

	public void loadData() {
		var tabs = getDatabase().getTabsDao().getAllTabs();
		this.tabs.addAll(stream(tabs).map(TabSetting::new).toList());
	}

	@Override
	public String getTitle(Context context) {
		return "Tabs";
	}

	@Override
	public List<SettingsItem> getHeaderItems() {
		return headerItems;
	}

	@Override
	public List<SettingsItem> getItems() {
		return tabs;
	}

	private static class TabSetting extends SettingsItem {
		private final DBTab tab;

		public TabSetting(DBTab tab) {
			super(SettingsItemType.ACTION);
			this.tab = tab;
		}

		@Override
		public String getTitle(Context context) {
			return tab.title;
		}

		@Override
		public boolean isDraggableInto(SettingsItem item) {
			return true;
		}

		@Override
		public boolean isDraggable() {
			return true;
		}

		@Override
		public boolean onDragged(long fromPosition, long toPosition) {
			return true;
		}
	}
}