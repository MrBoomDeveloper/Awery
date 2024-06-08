package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.getResourceId;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.WidgetIconEdittextBinding;
import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.ui.dialog.BaseDialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.IconPickerDialog;
import com.mrboomdev.awery.util.ui.dialog.SelectionDialog;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TabsSettings extends SettingsItem implements ObservableSettingsItem {
	private final Map<String, IconStateful> icons;
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
					var icon = new AtomicReference<>("catalog");

					new DialogBuilder(context)
							.setTitle("Create a tab")
							.addView(parent -> {
								binding.set(WidgetIconEdittextBinding.inflate(
										LayoutInflater.from(context),
										parent, false));

								binding.get().icon.setOnClickListener(v ->
										new IconPickerDialog<Map.Entry<String, IconStateful>>(context, Map.Entry::getValue)
												.setTitle("Select an icon")
												.setItems(icons.entrySet())
												.setSelectionListener(item -> {
													var id = getResourceId(R.drawable.class, item.getValue().getActive());
													var view = binding.get().icon;
													view.setImageResource(id);
													icon.set(item.getKey());
												}).show());

								binding.get().editText.setHint("Enter a name");
								return binding.get().getRoot();
							})
							.setNegativeButton(R.string.cancel, BaseDialogBuilder::dismiss)
							.setPositiveButton(R.string.create, dialog -> {
								var text = binding.get().editText.getText();

								if(text == null || text.toString().isBlank()) {
									binding.get().editText.setError("Tab name cannot be blank!");
									return;
								}

								new Thread(() -> {
									var dao = getDatabase().getTabsDao();

									var tab = new DBTab();
									tab.title = text.toString();
									tab.icon = icon.get();

									tab.index = stream(dao.getAllTabs())
											.mapToInt(item -> item.index)
											.max().orElse(0) + 1;

									dao.insert(tab);

									dialog.dismiss();
									toast("Tab created successfully!");
								}).start();
							})
							.show();
				}
			}
	);

	public TabsSettings() {
		try {
			var json = readAssets(new File("icons.json"));
			var adapter = Parser.<Map<String, IconStateful>>getAdapter(Map.class, String.class, IconStateful.class);
			icons = Parser.fromString(adapter, json);
		} catch(IOException e) {
			throw new RuntimeException("Failed to read an icons atlas!", e);
		}
	}

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
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_view_cozy);
			}

			@Override
			public void onClick(Context context) {
				new SelectionDialog<Selection.Selectable<String>>(context, SelectionDialog.Mode.SINGLE)
						.setTitle("Templates")
						.setNegativeButton(R.string.cancel, SelectionDialog::dismiss)
						.setPositiveButton(R.string.confirm, dialog -> {
							toast("This functionality isn't done yet!");
						})
						.show();
			}
		});

		var tabs = getDatabase().getTabsDao().getAllTabs();
		Collections.sort(tabs);

		if(!tabs.isEmpty()) {
			items.add(new SettingsItem.Builder(SettingsItemType.CATEGORY)
					.setTitle("Your tabs")
					.build());

			this.items.addAll(stream(tabs)
					.map(TabSetting::new)
					.toList());
		}
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

	private class TabSetting extends SettingsItem {
		private final DBTab tab;

		public TabSetting(DBTab tab) {
			super(SettingsItemType.ACTION);
			this.tab = tab;
		}

		@Override
		public String getTitle(Context context) {
			return tab.title;
		}

		@Nullable
		@Override
		public Drawable getIcon(@NonNull Context context) {
			var icon = icons.get(tab.icon);

			if(icon == null) {
				return null;
			}

			var id = getResourceId(R.drawable.class, icon.getActive());
			return ContextCompat.getDrawable(context, id);
		}

		@Override
		public boolean isDraggableInto(SettingsItem item) {
			return item instanceof TabSetting;
		}

		@Override
		public boolean isDraggable() {
			return true;
		}

		@Override
		public boolean onDragged(int fromPosition, int toPosition) {
			Collections.swap(items, fromPosition, toPosition);

			for(int i = 0; i < items.size(); i++) {
				if(items.get(i) instanceof TabSetting tabSetting) {
					tabSetting.tab.index = i;
				}
			}

			new Thread(() -> getDatabase().getTabsDao().insert(stream(items)
					.filter(item -> item instanceof TabSetting)
					.map(setting -> ((TabSetting)setting).tab)
					.toArray(DBTab[]::new))).start();

			return true;
		}
	}
}