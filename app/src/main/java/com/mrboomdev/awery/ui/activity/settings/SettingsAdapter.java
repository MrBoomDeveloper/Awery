package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.app.AweryLifecycle.restartApp;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setScale;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsData;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.ItemListSettingBinding;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.ui.popup.dialog.SelectionDialog;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogEditTextField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import java9.util.stream.Collectors;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private final WeakHashMap<SettingsItem, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private final SettingsDataHandler handler;
	private List<SettingsItem> items;

	public SettingsAdapter(SettingsItem data, SettingsDataHandler handler) {
		this.handler = handler;
		setHasStableIds(true);
		setData(data, false);
	}

	@SuppressLint("NotifyDataSetChanged")
	private void setData(@NonNull SettingsItem data, boolean notify) {
		if(data.getItems() == null) {
			this.items = Collections.emptyList();
			idGenerator.clear();

			if(notify) {
				notifyDataSetChanged();
			}

			return;
		}

		idGenerator.clear();

		this.items = new ArrayList<>(stream(data.getItems())
				.filter(SettingsItem::isVisible)
				.toList());

		for(var item : items) {
			ids.put(item, idGenerator.getLong());
		}

		if(data instanceof ObservableSettingsItem listenable) {
			listenable.setNewItemListener((setting, index) -> {
				ids.put(setting, idGenerator.getLong());
				items.add(setting);
				notifyItemInserted(items.indexOf(setting));
			});

			listenable.setRemovalItemListener((setting, index) -> {
				index = items.indexOf(setting);
				items.remove(setting);
				notifyItemRemoved(index);
			});

			listenable.setChangeItemListener((setting, index) -> {
				var oldSetting = items.set(index, setting);
				ids.put(setting, ids.get(oldSetting));
				notifyItemChanged(index);
			});
		}

		if(notify) {
			notifyDataSetChanged();
		}
	}

	@Override
	public long getItemId(int position) {
		var item = items.get(position);
		var id = ids.get(item);

		if(id == null) {
			throw new IllegalStateException("Id for item " + item + " not found");
		}

		return id;
	}

	public void setData(@NonNull SettingsItem data) {
		setData(data, true);
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var context = parent.getContext();
		var inflater = LayoutInflater.from(context);

		var binding = ItemListSettingBinding.inflate(inflater, parent, false);
		var holder = new ViewHolder(binding);

		ViewUtil.setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
			ViewUtil.setLeftPadding(binding.getRoot(), insets.left);
			ViewUtil.setRightPadding(binding.getRoot(), insets.right);
		}, parent);

		binding.getRoot().setOnClickListener(view -> {
			var setting = holder.getItem();
			if(setting == null) return;

			switch(setting.getType()) {
				case BOOLEAN -> binding.toggle.performClick();
				case SCREEN, SCREEN_BOOLEAN -> handler.onScreenLaunchRequest(setting);

				case ACTION -> {
					if(setting instanceof CustomSettingsItem) {
						setting.onClick(parent.getContext());
						return;
					}

					SettingsActions.run(setting.getFullKey());

					if(setting.isRestartRequired()) {
						suggestToRestart(parent);
					}
				}

				case STRING -> {
					var inputField = new DialogEditTextField(context);
					inputField.setImeFlags(EditorInfo.IME_ACTION_DONE);
					inputField.setText(setting.getStringValue());
					inputField.setLinesCount(1);

					var dialog = new DialogBuilder(context)
							.setTitle(setting.getTitle(context).trim())
							.setMessage(setting.getDescription(context).trim())
							.addView(inputField.getView())
							.setCancelButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.ok, _dialog -> {
								if(setting instanceof CustomSettingsItem custom) {
									custom.saveValue(inputField.getText());
								} else {
									var prefs = AwerySettings.getInstance(context);
									prefs.setString(setting.getFullKey(), inputField.getText());
									prefs.saveAsync();
								}

								_dialog.dismiss();
								setting.setStringValue(inputField.getText());

								if(setting.isRestartRequired()) {
									suggestToRestart(parent);
								}
							})
							.show();

					inputField.setCompletionCallback(dialog::performPositiveClick);
				}

				case INT -> {
					var inputField = new DialogEditTextField(context);
					inputField.setImeFlags(EditorInfo.IME_ACTION_DONE);
					inputField.setType(EditorInfo.TYPE_CLASS_NUMBER);
					inputField.setText(setting.getIntValue());
					inputField.setLinesCount(1);

					var dialog = new DialogBuilder(context)
							.setTitle(setting.getTitle(context).trim())
							.setMessage(setting.getDescription(context).trim())
							.addView(inputField.getView())
							.setCancelButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.ok, _dialog -> {
								if(inputField.getText().isBlank()) {
									inputField.setError("Text cannot be empty!");
									return;
								}

								try {
									var number = Integer.parseInt(inputField.getText());

									if(setting.isFromToAvailable()) {
										if(number > setting.getTo()) {
											inputField.setError("Value is too high. Max is: " + Math.round(setting.getTo()));
											return;
										}

										if(number < setting.getFrom()) {
											inputField.setError("Value is too low. Min is: " + Math.round(setting.getFrom()));
											return;
										}
									}

									var prefs = AwerySettings.getInstance();
									prefs.setInt(setting.getFullKey(), number);
									prefs.saveAsync();

									setting.setIntValue(number);
								} catch(NumberFormatException e) {
									inputField.setError("This is not a number!");
									return;
								}

								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(parent);
								}
							})
							.show();

					inputField.setCompletionCallback(dialog::performPositiveClick);
				}

				case SELECT, SELECT_INT -> {
					var dialog = new SelectionDialog<Selection.Selectable<String>>(context, SelectionDialog.Mode.SINGLE)
							.setTitle(setting.getTitle(context))
							.setCancelButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.confirm, (_dialog, selection) -> {
								if(setting instanceof CustomSettingsItem customSetting) {
									var item = selection.get(Selection.State.SELECTED);

									if(item == null) {
										_dialog.dismiss();
										return;
									}

									var id = item.getId();
									holder.updateDescription(id);
									customSetting.saveValue(id);

									_dialog.dismiss();

									if(setting.isRestartRequired()) {
										suggestToRestart(parent);
									}

									return;
								}

								if(setting.getBehaviour() != null) {
									SettingsData.saveSelectionList(setting.getBehaviour(), selection);
								} else {
									var item = selection.get(Selection.State.SELECTED);

									if(item == null) {
										_dialog.dismiss();
										return;
									}

									var id = item.getId();
									holder.updateDescription(id);

									var prefs = AwerySettings.getInstance();

									if(setting.getType() == SettingsItemType.SELECT_INT) {
										var integer = Integer.parseInt(id);
										setting.setIntValue(integer);
										prefs.setInt(setting.getFullKey(), integer);
									} else {
										setting.setStringValue(id);
										prefs.setString(setting.getFullKey(), id);
									}

									prefs.saveAsync();
								}

								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(parent);
								}
							})
							.show();

					if(setting.getBehaviour() != null) {
						SettingsData.getSelectionList(context, setting.getBehaviour(), (items, e) -> {
							if(!dialog.isShown()) return;

							if(e != null) {
								dialog.dismiss();
								AweryApp.toast(e.getMessage());
								return;
							}

							runOnUiThread(() -> dialog.setItems(items));
						});
					} else if(setting.getItems() != null && !setting.getItems().isEmpty()) {
						String selected;

						if(setting.getType() == SettingsItemType.SELECT_INT) {
							selected = String.valueOf(setting instanceof CustomSettingsItem customSetting
									? customSetting.getSavedValue()
									: setting.getIntValue());
						} else {
							selected = setting instanceof CustomSettingsItem customSetting
									? (String) customSetting.getSavedValue()
									: setting.getStringValue();
						}

						dialog.setItems(stream(setting.getItems())
								.map(item -> new Selection.Selectable<>(
										item.getTitle(context),
										item.getKey(),
										item.getKey().equals(selected) ? Selection.State.SELECTED : Selection.State.UNSELECTED
								)).collect(Selection.collect()));
					} else {
						throw new IllegalArgumentException("Failed to load items list");
					}
				}

				case MULTISELECT -> {
					var dialog = new SelectionDialog<Selection.Selectable<String>>(context, SelectionDialog.Mode.MULTI)
							.setTitle(setting.getTitle(parent.getContext()))
							.setCancelButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.confirm, (_dialog, selection) -> {
								var items = selection.getAll(Selection.State.SELECTED);
								if(items == null) return;

								if(setting instanceof CustomSettingsItem customSetting) {
									customSetting.saveValue(stream(items)
											.map(Selection.Selectable::getId)
											.collect(Collectors.toSet()));

									_dialog.dismiss();

									if(setting.isRestartRequired()) {
										suggestToRestart(parent);
									}

									return;
								}

								SettingsData.saveSelectionList(setting.getBehaviour(), selection);
								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(parent);
								}
							})
							.show();

					if(setting.getBehaviour() != null) {
						SettingsData.getSelectionList(context, setting.getBehaviour(), (items, e) -> {
							if(!dialog.isShown()) return;

							if(e != null) {
								dialog.dismiss();
								AweryApp.toast(e.getMessage());
								return;
							}

							runOnUiThread(() -> dialog.setItems(items));
						});
					} else if(setting.getItems() != null) {
						var selected = setting instanceof CustomSettingsItem customSetting
								? (Set<String>) customSetting.getSavedValue()
								: AwerySettings.getInstance().getStringSet(setting.getFullKey());

						dialog.setItems(stream(setting.getItems())
								.map(item -> new Selection.Selectable<>(
										item.getTitle(context),
										item.getKey(),
										selected.contains(item.getKey())
												? Selection.State.SELECTED
												: Selection.State.UNSELECTED
								)).collect(Selection.collect()));
					} else {
						throw new IllegalArgumentException("Failed to load items list");
					}
				}

				default -> throw new IllegalArgumentException("Unsupported setting type! " + setting.getType());
			}
		});

		binding.toggle.setOnCheckedChangeListener((view, isChecked) -> {
			var item = holder.getItem();
			if(item == null || !holder.didInit()) return;

			item.setBooleanValue(isChecked);
			handler.save(item, isChecked);
			holder.updateDescription(String.valueOf(isChecked));

			if(item.isRestartRequired()) {
				suggestToRestart(parent);
			}
		});

		return holder;
	}

	private void suggestToRestart(View parent) {
		Snackbar.make(parent, "Restart is required to apply changes", 2250)
				.setAction("Restart", _view -> restartApp()).show();
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemListSettingBinding binding;
		private final Context context;
		private SettingsItem item;
		private boolean didInit;

		public ViewHolder(@NonNull ItemListSettingBinding binding) {
			super(binding.getRoot());

			this.binding = binding;
			this.context = getContext(binding);
		}

		public SettingsItem getItem() {
			return item;
		}

		public boolean didInit() {
			return didInit;
		}

		public void updateDescription(String payload) {
			var description = resolveDescription(payload);
			setTopMargin(binding.title, dpPx(description == null ? 3 : -5));

			if(description == null) {
				binding.description.setVisibility(View.GONE);
			} else {
				binding.description.setVisibility(View.VISIBLE);
				binding.description.setText(description);
			}
		}

		@Nullable
		private String resolveDescription(String payload) {
			var result = item.getDescription(context);
			if(result == null) return null;

			if(result.contains("${VALUE_TITLE}")) {
				var prefs = payload == null ? AwerySettings.getInstance() : null;

				var value = switch(item.getType()) {
					case STRING -> payload != null ? payload : (item instanceof CustomSettingsItem customSetting
							? (String) customSetting.getSavedValue() : prefs.getString(item.getFullKey()));

					case BOOLEAN, SCREEN_BOOLEAN -> (payload != null
							? Boolean.parseBoolean(payload)
							: (item instanceof CustomSettingsItem customSetting
								? (Boolean) customSetting.getSavedValue()
								: prefs.getBoolean(item.getFullKey()))) ? "Enabled" : "Disabled";

					case INT -> payload != null ? payload : String.valueOf(prefs.getInt(item.getFullKey()));

					case SELECT -> {
						var selected = payload != null ? payload : (item instanceof CustomSettingsItem customSetting
								? (String) customSetting.getSavedValue() : prefs.getString(item.getFullKey()));

						if(item.getItems() == null) {
							yield "";
						}

						var found = stream(item.getItems()).filter(i -> i.getKey().equals(selected)).findFirst();
						yield found.isPresent() ? found.get().getTitle(context) : "";
					}

					case SELECT_INT -> {
						var selected = payload != null ? payload : String.valueOf(item instanceof CustomSettingsItem customSetting
								? customSetting.getSavedValue() : prefs.getInt(item.getFullKey()));

						var found = stream(item.getItems()).filter(i -> i.getKey().equals(selected)).findFirst();
						yield found.isPresent() ? found.get().getTitle(context) : "";
					}

					default -> "";
				};

				result = result.replaceAll("\\$\\{VALUE_TITLE\\}", value);
			}

			return result;
		}

		public void bind(@NonNull SettingsItem item) {
			this.didInit = false;
			this.item = item;

			binding.title.setText(item.getTitle(context));
			updateDescription(null);
			var icon = item.getIcon(context);

			if(icon == null) {
				binding.icon.setVisibility(View.GONE);
			} else {
				binding.icon.setVisibility(View.VISIBLE);
				binding.icon.setImageDrawable(icon);
				setScale(binding.icon, item.getIconSize());

				if(item.tintIcon()) {
					var context = binding.getRoot().getContext();
					var colorAttr = com.google.android.material.R.attr.colorOnSecondaryContainer;
					var color = AweryApp.resolveAttrColor(context, colorAttr);
					binding.icon.setImageTintList(ColorStateList.valueOf(color));
				} else {
					binding.icon.setImageTintList(null);
				}
			}

			if(item.getType() == SettingsItemType.BOOLEAN || item.getType() == SettingsItemType.SCREEN_BOOLEAN) {
				binding.toggle.setVisibility(View.VISIBLE);
				binding.toggle.setChecked(item.getBooleanValue());
			} else {
				binding.toggle.setVisibility(View.GONE);
			}

			this.didInit = true;
		}
	}
}