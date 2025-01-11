package com.mrboomdev.awery.ui.mobile.screens.settings;

import static com.mrboomdev.awery.app.App.snackbar;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.alwaysTrue;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.platform.PlatformResourcesKt.i18n;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.app.AweryLifecycle;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.WidgetIconEdittextBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.generated.Res;
import com.mrboomdev.awery.generated.String0_commonMainKt;
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupActivity;
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

import kotlin.Unit;

public class TabsSettings extends SettingsItem implements ObservableSettingsItem {
	private final Map<String, IconStateful> icons;
	private final List<SettingsItem> items = new ArrayList<>();
	private final List<DBTab> tabs = new ArrayList<>();
	private final List<SettingsItem> headerItems = List.of(
			new SettingsItem(SettingsItemType.ACTION) {

				@Override
				public Drawable getIcon(@NonNull Context context) {
					return ContextCompat.getDrawable(context, R.drawable.ic_add);
				}

				@Override
				public void onClick(Context context) {
					// TODO: Remove this temp block

					if(alwaysTrue()) {
						toast("This this isn't done yet. Come back later!");
						return;
					}

					var binding = new AtomicReference<WidgetIconEdittextBinding>();
					var icon = new AtomicReference<>("catalog");

					new DialogBuilder(context)
							.setTitle(R.string.create_tab)
							.addView(parent -> {
								binding.set(WidgetIconEdittextBinding.inflate(
										LayoutInflater.from(context),
										parent, false));

								binding.get().icon.setOnClickListener(v ->
										new IconPickerDialog<Map.Entry<String, IconStateful>>(context) {
											@Override
											public IconStateful getIcon(Map.Entry<String, IconStateful> item) {
												return item.getValue();
											}
										}
												.setTitle(R.string.select_icon)
												.setItems(icons.entrySet())
												.setSelectionListener(item -> {
													binding.get().icon.setImageResource(item.getValue().getResourceId(IconStateful.State.ACTIVE));
													icon.set(item.getKey());
												}).show());

								binding.get().editText.setHint(i18n(String0_commonMainKt.getEnter_text(Res.string.INSTANCE)));
								return binding.get().getRoot();
							})
							.setNegativeButton(i18n(String0_commonMainKt.getCancel(Res.string.INSTANCE)), BaseDialogBuilder::dismiss)
							.setPositiveButton(i18n(String0_commonMainKt.getCreate(Res.string.INSTANCE)), dialog -> {
								var text = binding.get().editText.getText();

								if(text == null || text.toString().isBlank()) {
									binding.get().editText.setError(i18n(String0_commonMainKt.getTab_name_empty_error(Res.string.INSTANCE)));
									return;
								}

								thread(() -> {
									var dao = App.Companion.getDatabase().getTabsDao();

									var tab = new DBTab();
									tab.title = text.toString();
									tab.icon = icon.get();

									tab.index = stream(dao.getAllTabs())
											.mapToInt(item -> item.index)
											.max().orElse(0) + 1;

									dao.insert(tab);
									getPrefs().setValue(AwerySettings.INSTANCE.getTABS_TEMPLATE().getKey(), "custom").saveAsync();

									runOnUiThread(() -> {
										if(items.size() == 2) {
											var title = new SettingsItem.Builder(SettingsItemType.CATEGORY)
													.setTitle(i18n(String0_commonMainKt.getCustom_tabs(Res.string.INSTANCE)))
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
	);

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
		items.add(new SettingsItem(SettingsItemType.SELECT) {

			@Override
			public String getKey() {
				return AwerySettings.INSTANCE.getDEFAULT_HOME_TAB().getKey();
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
			public List<? extends SettingsItem> getItems() {
				var savedTemplate = AwerySettings.INSTANCE.getTABS_TEMPLATE().getValue();

				if(!Objects.equals(savedTemplate, "custom")) {
					try {
						var templatesJson = readAssets("tabs_templates.json");
						var adapter = Parser.<List<TabsTemplate>>getAdapter(List.class, TabsTemplate.class);
						var templates = Parser.fromString(adapter, templatesJson);

						var selected = find(templates, template -> template.id.equals(savedTemplate));

						if(selected != null) {
							return stream(selected.tabs)
									.map(TabSetting::new)
									.toList();
						}
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				}

				if(tabs == null) {
					return Collections.emptyList();
				}

				return stream(tabs)
						.map(TabSetting::new)
						.toList();
			}
		});

		items.add(new CustomSettingsItem(SettingsItemType.ACTION) {

			@Override
			public String getTitle(Context context) {
				return context.getString(R.string.select_template);
			}

			@Override
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_view_cozy);
			}

			@Override
			public void onClick(Context context) {
				var intent = new Intent(context, SetupActivity.class);
				intent.putExtra(SetupActivity.EXTRA_STEP, SetupActivity.STEP_TEMPLATE);
				intent.putExtra(SetupActivity.EXTRA_FINISH_ON_COMPLETE, true);
				
				startActivityForResult(requireNonNull(getActivity(context)), intent, (resultCode, data) -> {
					if(resultCode != SetupActivity.RESULT_OK) return Unit.INSTANCE;
					
					for(int i = items.size() - 1; i >= 0; i--) {
						if(i <= 1) break;
						
						var setting = items.get(i);
						items.remove(setting);
						onSettingRemoval(setting);
					}
					
					snackbar(Objects.requireNonNull(getActivity(context)),
							R.string.restart_to_apply_settings, R.string.restart, AweryLifecycle::restartApp);
					
					return Unit.INSTANCE;
				});
			}
		});

		var tabs = App.Companion.getDatabase().getTabsDao().getAllTabs();
		Collections.sort(tabs);

		this.tabs.clear();
		this.tabs.addAll(tabs);

		if(!tabs.isEmpty()) {
			items.add(new SettingsItem.Builder(SettingsItemType.CATEGORY)
					.setTitle(R.string.custom_tabs)
					.build());

			this.items.addAll(stream(tabs)
					.map(TabSetting::new)
					.toList());
		}
	}

	@Override
	public String getTitle(@NonNull Context context) {
		return context.getString(R.string.tabs);
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

		private final List<SettingsItem> actions = List.of(new CustomSettingsItem(SettingsItemType.ACTION) {

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
							var tabsDao = App.Companion.getDatabase().getTabsDao();
							var feedsDao = App.Companion.getDatabase().getFeedsDao();

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
			
			return ContextCompat.getDrawable(context,
					icon.getResourceId(IconStateful.State.ACTIVE));
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

			thread(() -> App.Companion.getDatabase().getTabsDao().insert(stream(items)
					.filter(item -> item instanceof TabSetting)
					.map(setting -> ((TabSetting)setting).tab)
					.toArray(DBTab[]::new)));

			return true;
		}
	}
}