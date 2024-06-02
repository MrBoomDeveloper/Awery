package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.WidgetIconEdittextBinding;
import com.mrboomdev.awery.ui.popup.dialog.SelectionDialog;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogEditTextField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TabsSettings extends SettingsItem implements ObservableSettingsItem {
	private final List<SettingsItem> items = new ArrayList<>();
	private final List<SettingsItem> headerItems = List.of(
			new SettingsItem(SettingsItemType.ACTION) {
				@Override
				public Drawable getIcon(@NonNull Context context) {
					return ContextCompat.getDrawable(context, R.drawable.ic_add);
				}

				@Override
				public void onClick(Context context) {
					var binding = new AtomicReference<WidgetIconEdittextBinding>();

					new DialogBuilder(context)
							.setTitle("Create a tab")
							.addView(parent -> {
								binding.set(WidgetIconEdittextBinding.inflate(
										LayoutInflater.from(context),
										parent, false));

								binding.get().editText.setHint("Enter a name");
								return binding.get().getRoot();
							})
							.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
							.setPositiveButton(R.string.create, dialog -> {
								var text = binding.get().editText.getText();

								if(text == null || text.toString().isBlank()) {
									binding.get().editText.setError("Tab name cannot be blank!");
									return;
								}

								new Thread(() -> {
									dialog.dismiss();
									toast("Tab created successfully!");
								}).start();
							})
							.show();
				}
			}
	);

	public void loadData() {
		items.add(new SettingsItem(SettingsItemType.SELECT) {
			@Override
			public String getKey() {
				return "default_tab";
			}

			@Override
			public String getFullKey() {
				return AwerySettings.ui.DEFAULT_HOME_TAB;
			}

			@Override
			public String getTitle(Context context) {
				return context.getString(R.string.startUpTab);
			}

			@Override
			public String getDescription(Context context) {
				return context.getString(R.string.default_tab_description);
			}

			@Override
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_home_filled);
			}

			@Override
			public List<SettingsItem> getItems() {
				return Collections.emptyList();
			}
		});

		items.add(new CustomSettingsItem(SettingsItemType.ACTION) {
			@Override
			public String getTitle(Context context) {
				return "Select a template";
			}

			@Override
			public void onClick(Context context) {
				new SelectionDialog<Selection.Selectable<String>>(context, SelectionDialog.Mode.SINGLE)
						.setTitle("Templates")
						.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
						.setPositiveButton(R.string.confirm, dialog -> {
							toast("This functionality isn't done yet!");
						})
						.show();
			}
		});

		var tabs = getDatabase().getTabsDao().getAllTabs();

		if(!tabs.isEmpty()) {
			items.add(new SettingsItem(SettingsItemType.CATEGORY) {
				@Override
				public String getTitle(Context context) {
					return "Your tabs";
				}
			});
		}

		this.items.addAll(stream(tabs).map(TabSetting::new).toList());
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
		return items;
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