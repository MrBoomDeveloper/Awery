package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.App.getResourceId;
import static com.mrboomdev.awery.app.App.i18n;
import static com.mrboomdev.awery.app.App.snackbar;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getActivity;
import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.Lifecycle.getAppContext;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.app.data.Constants.alwaysTrue;
import static com.mrboomdev.awery.app.data.db.AweryDB.getDatabase;
import static com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.Lifecycle;
import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.app.data.db.item.DBTab;
import com.mrboomdev.awery.app.data.settings.base.ParsedSetting;
import com.mrboomdev.awery.app.data.settings.base.CustomSettingsItem;
import com.mrboomdev.awery.app.data.settings.base.SettingsItemType;
import com.mrboomdev.awery.databinding.WidgetIconEdittextBinding;
import com.mrboomdev.awery.ext.data.Image;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.activity.setup.SetupActivity;
import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TabsTemplate;
import com.mrboomdev.awery.util.ui.dialog.BaseDialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.IconPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TabsSettings extends Setting {
	private static final int REQUEST_CODE_SETUP = Lifecycle.getActivityResultCode();
	private final Map<String, IconStateful> icons;
	private final List<Setting> items = new ArrayList<>();
	private final List<DBTab> tabs = new ArrayList<>();

	private final Setting[] headerItems = {
			new Setting(Setting.Type.ACTION) {
				@Override
				public Image getIcon() {
					return new AndroidImage(R.drawable.ic_add);
				}

				@Override
				public void onClick() {
					// TODO: Remove this temp block

					if(alwaysTrue()) {
						toast("This this isn't done yet. Come back later!");
						return;
					}

					var context = getAnyActivity(AppCompatActivity.class);
					var binding = new AtomicReference<WidgetIconEdittextBinding>();
					var icon = new AtomicReference<>("catalog");

					new DialogBuilder(context)
							.setTitle(R.string.create_tab)
							.addView(parent -> {
								binding.set(WidgetIconEdittextBinding.inflate(
										LayoutInflater.from(context),
										parent, false));

								binding.get().icon.setOnClickListener(v ->
										new IconPickerDialog<Map.Entry<String, IconStateful>>(context, Map.Entry::getValue)
												.setTitle(R.string.select_icon)
												.setItems(icons.entrySet())
												.setSelectionListener(item -> {
													var id = getResourceId(R.drawable.class, item.getValue().getActive());
													var view = binding.get().icon;
													view.setImageResource(id);
													icon.set(item.getKey());
												}).show());

								binding.get().editText.setHint(R.string.enter_text);
								return binding.get().getRoot();
							})
							.setNegativeButton(R.string.cancel, BaseDialogBuilder::dismiss)
							.setPositiveButton(R.string.create, dialog -> {
								var text = binding.get().editText.getText();

								if(text == null || text.toString().isBlank()) {
									binding.get().editText.setError(getAppContext().getString(R.string.tab_name_empty_error));
									return;
								}

								thread(() -> {
									var dao = getDatabase().getTabsDao();

									var tab = new DBTab();
									tab.title = text.toString();
									tab.icon = icon.get();

									tab.index = stream(dao.getAllTabs())
											.mapToInt(item -> item.index)
											.max().orElse(0) + 1;

									dao.insert(tab);
									getPrefs().setValue(AwerySettings.TABS_TEMPLATE, "custom").saveAsync();

									runOnUiThread(() -> {
										if(items.size() == 2) {
											var title = new Setting.Builder(Setting.Type.CATEGORY)
													.setTitle(getAppContext().getString(R.string.custom_tabs))
													.build();

											items.add(title);
											onSettingAddition(title, 2);
										}

										var newSetting = new TabSetting(tab);
										items.add(newSetting);
										tabs.add(tab);
										onSettingAddition(newSetting, items.size() - 1);

										dialog.dismiss();
										toast("Tab created successfully!");
									});
								});
							})
							.show();
				}
			}
	};

	public TabsSettings() {
		try {
			var json = readAssets("icons.json");
			var adapter = Parser.<Map<String, IconStateful>>getAdapter(Map.class, String.class, IconStateful.class);
			icons = Parser.fromString(adapter, json);
		} catch(IOException e) {
			throw new RuntimeException("Failed to read an icons atlas!", e);
		}
	}

	public void loadData() {
		items.add(new Setting(Setting.Type.SELECT) {

			@Override
			public String getKey() {
				return AwerySettings.DEFAULT_HOME_TAB.getKey();
			}

			@Override
			public String getTitle() {
				return getAppContext().getString(R.string.startUpTab);
			}

			@Override
			public String getDescription() {
				return getAppContext().getString(R.string.default_tab_description);
			}

			@Override
			public Image getIcon() {
				return new AndroidImage(R.drawable.ic_home_filled);
			}

			@Override
			public Setting[] getItems() {
				var savedTemplate = AwerySettings.TABS_TEMPLATE.getValue();

				if(!Objects.equals(savedTemplate, "custom")) {
					try {
						var templatesJson = readAssets("tabs_templates.json");
						var adapter = Parser.<List<TabsTemplate>>getAdapter(List.class, TabsTemplate.class);
						var templates = Parser.fromString(adapter, templatesJson);

						var selected = find(templates, template -> template.id.equals(savedTemplate));

						if(selected != null) {
							return stream(selected.tabs)
									.map(TabSetting::new)
									.toArray(Setting[]::new);
						}
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				}

				return stream(tabs)
						.map(TabSetting::new)
						.toArray(Setting[]::new);
			}
		});

		items.add(new ParsedSetting(Setting.Type.ACTION) {

			@Override
			public String getTitle() {
				return i18n(R.string.select_template);
			}

			@Override
			public Image getIcon() {
				return new AndroidImage(R.drawable.ic_view_cozy);
			}

			@Override
			public void onClick() {
				var context = getAnyActivity(AppCompatActivity.class);
				var intent = new Intent(context, SetupActivity.class);
				intent.putExtra(SetupActivity.EXTRA_STEP, SetupActivity.STEP_TEMPLATE);
				intent.putExtra(SetupActivity.EXTRA_FINISH_ON_COMPLETE, true);

				startActivityForResult(context, intent, REQUEST_CODE_SETUP, (resultCode, result) -> {
					if(resultCode != SetupActivity.RESULT_OK) return;

					for(int i = items.size() - 1; i >= 0; i--) {
						if(i <= 1) break;

						var setting = items.get(i);
						items.remove(setting);
						//onSettingRemoval(setting);
					}

					snackbar(Objects.requireNonNull(getActivity(context)),
							R.string.restart_to_apply_settings, R.string.restart, Lifecycle::restartApp);
				});
			}
		});

		var tabs = getDatabase().getTabsDao().getAllTabs();
		Collections.sort(tabs);

		this.tabs.clear();
		this.tabs.addAll(tabs);

		if(!tabs.isEmpty()) {
			items.add(new Setting.Builder(Setting.Type.CATEGORY)
					.setTitle(i18n(R.string.custom_tabs))
					.build());

			this.items.addAll(stream(tabs)
					.map(TabSetting::new)
					.toList());
		}
	}

	@Override
	public String getTitle() {
		return getAppContext().getString(R.string.tabs);
	}

	@Override
	public Setting[] getHeaderItems() {
		return headerItems;
	}

	@Override
	public Setting[] getItems() {
		return items.toArray(new Setting[0]);
	}

	private class TabSetting extends Setting {
		private final DBTab tab;

		private final Setting[] actions = List.of(new CustomSettingsItem(SettingsItemType.ACTION) {

			@Override
			public String getTitle(Context context) {
				return context.getString(R.string.delete);
			}

			@Override
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_delete_outlined);
			}

			@Override
			public void onClick(Context context) {
				new DialogBuilder(context)
						.setTitle(R.string.are_you_sure)
						.setMessage("You won't be able to revert the deletion.")
						.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
						.setPositiveButton(R.string.confirm, dialog -> thread(() -> {
							var tabsDao = getDatabase().getTabsDao();
							var feedsDao = getDatabase().getFeedsDao();

							for(var feed : feedsDao.getAllFromTab(tab.id)) {
								feedsDao.delete(feed);
							}

							tabsDao.delete(tab);

							runOnUiThread(() -> {
								items.remove(TabSetting.this);
								tabs.remove(tab);
								onSettingRemoval(TabSetting.this);

								if(items.size() == 3) {
									onSettingRemoval(items.remove(2));
								}

								dialog.dismiss();
							});
						})).show();
			}
		});

		public TabSetting(DBTab tab) {
			super(SettingsItemType.ACTION);
			this.tab = tab;
		}

		@Override
		public String getTitle(Context context) {
			return tab.title;
		}

		@Override
		public String getKey() {
			return tab.id;
		}

		@Override
		public List<SettingsItem> getActionItems() {
			return actions;
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

			thread(() -> getDatabase().getTabsDao().insert(stream(items)
					.filter(item -> item instanceof TabSetting)
					.map(setting -> ((TabSetting)setting).tab)
					.toArray(DBTab[]::new)));

			return true;
		}
	}
}