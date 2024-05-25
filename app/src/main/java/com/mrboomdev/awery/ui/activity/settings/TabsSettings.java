package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.toast;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;

import java.util.ArrayList;
import java.util.List;

public class TabsSettings extends SettingsItem {
	private List<SettingsItem> tabs = new ArrayList<>();
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

	public TabsSettings() {
		tabs.addAll(List.of(
				new SettingsItem(SettingsItemType.ACTION) {
					@Override
					public String getTitle(Context context) {
						return "Anime";
					}
				},

				new SettingsItem(SettingsItemType.ACTION) {
					@Override
					public String getTitle(Context context) {
						return "Library";
					}
				}
		));
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

	@Override
	public boolean isReordable() {
		return true;
	}
}