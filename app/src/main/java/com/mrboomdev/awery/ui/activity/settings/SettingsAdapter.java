package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.snackbar;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.NiceUtils.with;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setScale;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalMargin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryLifecycle;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsData;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.ItemListSettingBinding;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.SelectionDialog;
import com.mrboomdev.awery.util.ui.fields.EditTextField;
import com.mrboomdev.awery.util.ui.fields.FancyField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import java9.util.stream.Collectors;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private final WeakHashMap<SettingsItem, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private final SettingsDataHandler handler;
	private List<SettingsItem> items;
	private SettingsItem screen;

	public SettingsAdapter(SettingsItem screen, SettingsDataHandler handler) {
		this.handler = handler;
		setHasStableIds(true);
		setScreen(screen, false);
	}

	public SettingsItem getScreen() {
		return screen;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setScreen(@NonNull SettingsItem screen, boolean notify) {
		this.screen = screen;

		if(screen.getItems() == null) {
			this.items = Collections.emptyList();
			idGenerator.clear();

			if(notify) {
				notifyDataSetChanged();
			}

			return;
		}

		idGenerator.clear();

		this.items = new ArrayList<>(stream(screen.getItems())
				.filter(SettingsItem::isVisible)
				.toList());

		for(var item : items) {
			var id = idGenerator.getLong();
			ids.put(item, id);
		}

		if(screen instanceof ObservableSettingsItem listenable) {
			listenable.addSettingAdditionListener((setting, index) -> {
				var id = idGenerator.getLong();
				ids.put(setting, id);

				items.add(index, setting);
				notifyItemInserted(items.indexOf(setting));
			});

			listenable.addSettingRemovalListener((setting, index) -> {
				index = items.indexOf(setting);
				items.remove(setting);
				notifyItemRemoved(index);
			});

			listenable.addSettingChangeListener((setting, index) -> {
				var oldSetting = items.set(index, setting);

				var id = ids.get(oldSetting);
				ids.put(setting, id);

				notifyItemChanged(index);
			});
		}

		if(notify) {
			notifyDataSetChanged();
		}
	}

	public List<SettingsItem> getItems() {
		return items;
	}

	public void addItems(@NonNull List<SettingsItem> items) {
		int wasSize = this.items.size();

		var filtered = new ArrayList<>(stream(items)
				.filter(SettingsItem::isVisible)
				.toList());

		this.items.addAll(filtered);

		for(var item : filtered) {
			var id = idGenerator.getLong();
			ids.put(item, id);
		}

		notifyItemRangeInserted(wasSize, filtered.size());
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

	public void setScreen(@NonNull SettingsItem data) {
		setScreen(data, true);
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var context = parent.getContext();
		var inflater = LayoutInflater.from(context);

		var binding = ItemListSettingBinding.inflate(inflater, parent, false);
		var holder = new ViewHolder(binding);

		setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
			setRightPadding(binding.getRoot(), insets.right);
			return false;
		});

		binding.getRoot().setOnClickListener(view -> {
			var setting = holder.getItem();
			if(setting == null) return;

			switch(setting.getType()) {
				case BOOLEAN -> binding.toggle.performClick();
				case SCREEN, SCREEN_BOOLEAN -> handler.onScreenLaunchRequest(setting);

				case ACTION -> {
					setting.onClick(parent.getContext());
					holder.updateDescription(null);

					if(setting.isRestartRequired()) {
						suggestToRestart(parent);
					}
				}

				case STRING -> {
					setting.restoreSavedValues();

					var inputField = new EditTextField(context);
					inputField.setImeFlags(EditorInfo.IME_ACTION_DONE);
					inputField.setText(setting.getStringValue());
					inputField.setLinesCount(1);

					var dialog = new DialogBuilder(context)
							.setTitle(setting.getTitle(context).trim())
							.setMessage(setting.getDescription(context).trim())
							.addView(inputField.getView())
							.setNegativeButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.ok, _dialog -> {
								handler.save(setting, inputField.getText());
								setting.setValue(inputField.getText());

								if(setting.isRestartRequired()) {
									suggestToRestart(parent);
								}

								_dialog.dismiss();
							})
							.show();

					inputField.setCompletionListener(dialog::performPositiveClick);
				}

				case INTEGER -> {
					Object field;

					if(setting.isFromToAvailable()) {
						var slider = new Slider(context);
						slider.setValueFrom(setting.getFrom());
						slider.setValueTo(setting.getTo());
						slider.setStepSize(1);
						slider.setValue(Objects.requireNonNullElse(setting.getIntegerValue(), 0));
						field = slider;
					} else {
						var inputField = new EditTextField(context);
						inputField.setImeFlags(EditorInfo.IME_ACTION_DONE);
						inputField.setType(EditorInfo.TYPE_CLASS_NUMBER);
						inputField.setText(setting.getIntegerValue());
						inputField.setLinesCount(1);
						field = inputField;
					}

					var dialog = new DialogBuilder(context)
							.setTitle(setting.getTitle(context).trim())
							.setMessage(setting.getDescription(context).trim())
							.addView(field instanceof FancyField<?> fancy ? fancy.getView() : (View) field)
							.setNegativeButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.ok, _dialog -> {
								if(field instanceof EditTextField inputField) {
									if(inputField.getText().isBlank()) {
										inputField.setError(R.string.text_cant_empty);
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

										handler.save(setting, number);
										setting.setValue(number);
									} catch(NumberFormatException e) {
										inputField.setError(R.string.this_not_number);
										return;
									}
								} else {
									var slider = (Slider) field;
									handler.save(setting, (int) slider.getValue());
									setting.setValue((int) slider.getValue());
								}

								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(parent);
								}
							})
							.show();

					if(field instanceof EditTextField inputField) {
						inputField.setCompletionListener(dialog::performPositiveClick);
					}
				}

				case SELECT, SELECT_INTEGER -> {
					var dialog = new SelectionDialog<Selection.Selectable<String>>(context, SelectionDialog.Mode.SINGLE)
							.setTitle(setting.getTitle(context))
							.setNegativeButton(R.string.cancel, SelectionDialog::dismiss)
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

									if(setting.getType() == SettingsItemType.SELECT_INTEGER) {
										var integer = Integer.parseInt(id);
										handler.save(setting, integer);
										setting.setValue(integer);
									} else {
										handler.save(setting, id);
										setting.setValue(id);
									}
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
								toast(e.getMessage());
								return;
							}

							runOnUiThread(() -> dialog.setItems(items));
						});
					} else if(setting.getItems() != null) {
						String selected;

						if(setting.getType() == SettingsItemType.SELECT_INTEGER) {
							selected = String.valueOf(setting instanceof CustomSettingsItem customSetting
									? customSetting.getSavedValue()
									: setting.getIntegerValue());
						} else {
							setting.restoreSavedValues();

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
							.setNegativeButton(R.string.cancel, SelectionDialog::dismiss)
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

								handler.save(setting, selection);
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
								toast(e.getMessage());
								return;
							}

							runOnUiThread(() -> dialog.setItems(items));
						});
					} else if(setting.getItems() != null) {
						var selected = setting instanceof CustomSettingsItem customSetting
								? (Set<String>) customSetting.getSavedValue()
								: getPrefs().getStringSet(setting.getKey());

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

			item.setValue(isChecked);
			handler.save(item, isChecked);
			holder.updateDescription(String.valueOf(isChecked));

			if(item.isRestartRequired()) {
				suggestToRestart(parent);
			}
		});

		return holder;
	}

	private void suggestToRestart(View parent) {
		snackbar(Objects.requireNonNull(getActivity(getContext(parent))),
				R.string.restart_to_apply_settings, R.string.restart, AweryLifecycle::restartApp);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemListSettingBinding binding;
		private final Context context;
		private SettingsItem item;
		private boolean didInit;

		public ViewHolder(@NonNull ItemListSettingBinding binding) {
			super(binding.getRoot());

			this.binding = binding;
			this.context = getContext(binding);
		}

		public long getId() {
			var id = ids.get(item);

			if(id == null) {
				throw new IllegalStateException("Id for item " + item + " not found");
			}

			return id;
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
				var prefs = payload == null ? getPrefs() : null;

				var value = switch(item.getType()) {
					case STRING -> payload != null ? payload : (item instanceof CustomSettingsItem customSetting
							? (String) customSetting.getSavedValue() : prefs.getString(item.getKey()));

					case BOOLEAN, SCREEN_BOOLEAN -> context.getString((payload != null
							? Boolean.parseBoolean(payload)
							: (item instanceof CustomSettingsItem customSetting
								? (Boolean) customSetting.getSavedValue()
								: prefs.getBoolean(item.getKey()))) ? R.string.enabled : R.string.disabled);

					case INTEGER -> payload != null ? payload : String.valueOf(prefs.getInteger(item.getKey()));

					case SELECT -> {
						var selected = payload != null ? payload : (item instanceof CustomSettingsItem customSetting
								? (String) customSetting.getSavedValue() : prefs.getString(item.getKey()));

						if(item.getItems() == null) {
							yield "";
						}

						var found = stream(item.getItems()).filter(i -> i.getKey().equals(selected)).findFirst();
						yield found.isPresent() ? found.get().getTitle(context) : "";
					}

					case SELECT_INTEGER -> {
						var selected = payload != null ? payload : String.valueOf(item instanceof CustomSettingsItem customSetting
								? customSetting.getSavedValue() : prefs.getInteger(item.getKey()));

						var found = stream(item.getItems()).filter(i -> i.getKey().equals(selected)).findFirst();
						yield found.isPresent() ? found.get().getTitle(context) : "";
					}

					default -> "";
				};

				result = result.replaceAll("\\$\\{VALUE_TITLE\\}", value);
			}

			return result;
		}

		public void bind(@NonNull SettingsItem setting) {
			this.didInit = false;
			this.item = setting;

			var isSmall = setting.getType() == SettingsItemType.CATEGORY
					|| setting.getType() == SettingsItemType.DIVIDER;

			with(setting.getTitle(context), title -> {
				if(title == null) {
					binding.title.setText(null);
					return;
				}

				binding.title.setTextColor(resolveAttrColor(context, setting.getType() == SettingsItemType.CATEGORY
						? com.google.android.material.R.attr.colorOnSecondaryContainer
						: com.google.android.material.R.attr.colorOnBackground));

				binding.title.setTextSize(TypedValue.COMPLEX_UNIT_SP,
						(setting.getType() == SettingsItemType.CATEGORY) ? 14 : 16);

				binding.title.setText(title);
			});

			with(setting.getActionItems(), items -> {
				if(items == null || items.isEmpty()) {
					binding.options.setVisibility(View.GONE);
					return;
				}

				if(items.size() > 1) {
					binding.options.setImageResource(R.drawable.ic_round_dots_vertical_24);

					binding.options.setOnClickListener(v -> {
						var menu = new PopupMenu(context, binding.getRoot());

						for(int i = 0; i < items.size(); i++) {
							menu.getMenu().add(0, i, 0, items.get(i).getTitle(context));
						}

						menu.setOnMenuItemClickListener(item -> {
							items.get(item.getItemId()).onClick(context);
							return true;
						});

						menu.show();
					});
				} else {
					var action = items.get(0);
					binding.options.setImageDrawable(action.getIcon(context));
					binding.options.setOnClickListener(v -> action.onClick(context));
				}

				binding.options.setVisibility(View.VISIBLE);
			});

			with(setting.getIcon(context), icon -> {
				if(icon == null) {
					binding.icon.setVisibility(View.GONE);
					return;
				}

				binding.icon.setVisibility(View.VISIBLE);
				binding.icon.setImageDrawable(icon);
				setScale(binding.icon, setting.getIconSize());

				if(setting.tintIcon()) {
					var context = binding.getRoot().getContext();
					var colorAttr = com.google.android.material.R.attr.colorOnSecondaryContainer;
					var color = resolveAttrColor(context, colorAttr);
					binding.icon.setImageTintList(ColorStateList.valueOf(color));
				} else {
					binding.icon.setImageTintList(null);
				}
			});

			updateDescription(null);

			binding.divider.setVisibility(setting.getType() == SettingsItemType.DIVIDER ? View.VISIBLE : View.GONE);

			if(setting.getType() == SettingsItemType.CATEGORY) setVerticalMargin(binding.getRoot(), dpPx(-8), dpPx(-12));
			else if(setting.getType() == SettingsItemType.DIVIDER) setVerticalMargin(binding.getRoot(), dpPx(-12));
			else setVerticalMargin(binding.getRoot(), 0, dpPx(6));

			binding.getRoot().setMinimumHeight(dpPx(isSmall ? 0 : 54));
			binding.getRoot().setClickable(!isSmall);
			binding.getRoot().setFocusable(!isSmall);

			if(setting.getType() == SettingsItemType.BOOLEAN || setting.getType() == SettingsItemType.SCREEN_BOOLEAN) {
				binding.toggle.setVisibility(View.VISIBLE);
				binding.toggle.setChecked(setting.getBooleanValue());
			} else {
				binding.toggle.setVisibility(View.GONE);
			}

			this.didInit = true;
		}
	}
}