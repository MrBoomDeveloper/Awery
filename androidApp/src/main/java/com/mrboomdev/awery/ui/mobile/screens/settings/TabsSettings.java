package com.mrboomdev.awery.ui.mobile.screens.settings;

import static com.mrboomdev.awery.app.App.snackbar;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.alwaysTrue;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.platform.PlatformResourcesKt.i18n;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.utils.FileExtensionsAndroid.readAssets;
import static com.mrboomdev.awery.utils.IntentUtilsKt.buildIntent;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.WidgetIconEdittextBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.generated.Res;
import com.mrboomdev.awery.generated.String0_commonMainKt;
import com.mrboomdev.awery.platform.android.AndroidGlobals;
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupActivity;
import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TabsTemplate;
import com.mrboomdev.awery.util.ui.dialog.BaseDialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.IconPickerDialog;
import com.mrboomdev.awery.utils.ActivityUtilsKt;
import com.mrboomdev.awery.utils.ContextUtilsKt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Unit;
import kotlin.jvm.JvmClassMappingKt;

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
						AndroidGlobals.INSTANCE.toast("This this isn't done yet. Come back later!", 0);
						return;
					}

					var binding = new AtomicReference<WidgetIconEdittextBinding>();
					var icon = new AtomicReference<>("catalog");

					new DialogBuilder(context)
							.setTitle(i18n(String0_commonMainKt.getCreate_tab(Res.string.INSTANCE)))
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
												.setTitle(i18n(String0_commonMainKt.getSelect_icon(Res.string.INSTANCE)))
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
										AndroidGlobals.INSTANCE.toast("Tab created successfully!", 0);
									});
								});
							})
							.show();
				}
			}
	);

	public TabsSettings() {
		try {
			var adapter = Parser.<Map<String, IconStateful>>getAdapter(Map.class, String.class, IconStateful.class);
			icons = Parser.fromString(adapter, readAssets("icons.json"));
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
				return i18n(String0_commonMainKt.getStartUpTab(Res.string.INSTANCE));
			}

			@Override
			public String getDescription(Context context) {
				return i18n(String0_commonMainKt.getDefault_tab_description(Res.string.INSTANCE));
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
						var adapter = Parser.<List<TabsTemplate>>getAdapter(List.class, TabsTemplate.class);
						var templates = Parser.fromString(adapter, readAssets("tabs_templates.json"));
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
				return i18n(String0_commonMainKt.getSelect_template(Res.string.INSTANCE));
			}

			@Override
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_view_cozy);
			}

			@Override
			public void onClick(Context context) {
				ActivityUtilsKt.startActivityForResult(ContextUtilsKt.getActivity(context), buildIntent(
						context,
						JvmClassMappingKt.getKotlinClass(SetupActivity.class),
						new SetupActivity.Extras(SetupActivity.STEP_TEMPLATE, true),
						null,
						null,
						null,
						null),
						(resultCode, data) -> {
					if(resultCode != SetupActivity.RESULT_OK) return Unit.INSTANCE;
					
					for(int i = items.size() - 1; i >= 0; i--) {
						if(i <= 1) break;
						
						var setting = items.get(i);
						items.remove(setting);
						onSettingRemoval(setting);
					}
					
					snackbar(Objects.requireNonNull(getActivity(context)),
							i18n(String0_commonMainKt.getRestart_to_apply_settings(Res.string.INSTANCE)), i18n(String0_commonMainKt.getRestart(Res.string.INSTANCE)), () -> AndroidGlobals.INSTANCE.restartApp());
					
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
					.setTitle(i18n(String0_commonMainKt.getCustom_tabs(Res.string.INSTANCE)))
					.build());

			this.items.addAll(stream(tabs)
					.map(TabSetting::new)
					.toList());
		}
	}

	@Override
	public String getTitle(@NonNull Context context) {
		return i18n(String0_commonMainKt.getTabs(Res.string.INSTANCE));
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
				return i18n(String0_commonMainKt.getDelete(Res.string.INSTANCE));
			}

			@Override
			public Drawable getIcon(@NonNull Context context) {
				return ContextCompat.getDrawable(context, R.drawable.ic_delete_outlined);
			}

			@Override
			public void onClick(Context context) {
				new DialogBuilder(context)
						.setTitle(i18n(String0_commonMainKt.getAre_you_sure(Res.string.INSTANCE)))
						.setMessage("You won't be able to revert the deletion.")
						.setNegativeButton(i18n(String0_commonMainKt.getCancel(Res.string.INSTANCE)), DialogBuilder::dismiss)
						.setPositiveButton(i18n(String0_commonMainKt.getDelete(Res.string.INSTANCE)), dialog -> thread(() -> {
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