package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.snackbar;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.isTrue;
import static com.mrboomdev.awery.util.NiceUtils.isUrlValid;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.NiceUtils.with;
import static com.mrboomdev.awery.util.ui.ViewUtil.clearImageTint;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setImageTintAttr;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setScale;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalMargin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.checkbox.MaterialCheckBox;
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

	public SettingsAdapter(SettingsItem screen, @Nullable SettingsDataHandler handler) {
		this.handler = handler;
		setHasStableIds(true);
		setScreen(screen, false);
	}

	public SettingsItem getScreen() {
		return screen;
	}

	public void onEmptyStateChanged(boolean isEmpty) {}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(@NonNull List<SettingsItem> items, boolean notify) {
		this.screen = null;

		for(var item : items) {
			item.restoreSavedValues(handler != null ? handler : getPrefs());
		}

		idGenerator.clear();

		this.items = new ArrayList<>(stream(items)
				.filter(SettingsItem::isVisible)
				.toList());

		for(var item : items) {
			var id = idGenerator.getLong();
			ids.put(item, id);
		}

		if(notify) {
			notifyDataSetChanged();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setScreen(@NonNull SettingsItem screen, boolean notify) {
		this.screen = screen;
		screen.restoreSavedValues(handler != null ? handler : getPrefs());

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
			listenable.addSettingsObserver(new ObservableSettingsItem.Observer() {
				@Override
				public void onSettingAddition(SettingsItem item, int position) {
					var id = idGenerator.getLong();
					ids.put(item, id);

					var wasSize = items.size();
					items.add(position, item);
					notifyItemInserted(items.indexOf(item));

					if(wasSize == 0) {
						onEmptyStateChanged(false);
					}
				}

				@Override
				public void onSettingRemoval(SettingsItem item) {
					var index = items.indexOf(item);
					items.remove(item);
					notifyItemRemoved(index);

					if(items.isEmpty()) {
						onEmptyStateChanged(true);
					}
				}

				@Override
				public void onSettingChange(SettingsItem newItem, SettingsItem oldItem) {
					var index = items.indexOf(oldItem);

					if(newItem != oldItem) {
						items.set(index, newItem);

						ids.remove(oldItem);
						ids.put(newItem, ids.get(oldItem));
					}

					notifyItemChanged(index);
				}
			});
		}

		if(notify) {
			notifyDataSetChanged();
		}
	}

	public List<SettingsItem> getItems() {
		return items;
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
			setRightPadding(binding.getRoot(), isLandscape(context) ? insets.right : dpPx(binding, 16));
			return false;
		}, parent);

		binding.getRoot().setOnClickListener(view -> {
			var setting = holder.getItem();
			if(setting == null) return;

			switch(setting.getType()) {
				case BOOLEAN -> binding.toggle.performClick();
				case EXCLUDABLE -> binding.checkbox.performClick();
				case SCREEN, SCREEN_BOOLEAN -> handler.onScreenLaunchRequest(setting);

				case ACTION -> {
					setting.onClick(parent.getContext());
					holder.updateDescription(null);

					if(setting.isRestartRequired()) {
						suggestToRestart(context);
					}
				}

				case STRING -> {
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
								handler.saveValue(setting, inputField.getText());
								setting.setValue(inputField.getText());

								if(setting.isRestartRequired()) {
									suggestToRestart(context);
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

										handler.saveValue(setting, number);
										setting.setValue(number);
									} catch(NumberFormatException e) {
										inputField.setError(R.string.this_not_number);
										return;
									}
								} else {
									var slider = (Slider) field;
									handler.saveValue(setting, (int) slider.getValue());
									setting.setValue((int) slider.getValue());
								}

								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(context);
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
										suggestToRestart(context);
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
										handler.saveValue(setting, integer);
										setting.setValue(integer);
									} else {
										handler.saveValue(setting, id);
										setting.setValue(id);
									}
								}

								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(context);
								}
							}).show();

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
							selected = setting instanceof CustomSettingsItem customSetting
									? (String) customSetting.getSavedValue()
									: setting.getStringValue();
						}

						dialog.setItems(stream(setting.getItems())
								.filter(SettingsItem::isVisible)
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
										suggestToRestart(context);
									}

									return;
								}

								handler.saveValue(setting, selection);
								_dialog.dismiss();

								if(setting.isRestartRequired()) {
									suggestToRestart(context);
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
								: setting.getStringSetValue();

						dialog.setItems(stream(setting.getItems())
								.filter(SettingsItem::isVisible)
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

		return holder;
	}

	private static void suggestToRestart(Context context) {
		snackbar(Objects.requireNonNull(getActivity(context)),
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

	public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
		private final ItemListSettingBinding binding;
		private final Context context;
		private SettingsItem item;
		private boolean checkboxListenersActive = true;
		private boolean didUpdateExcludableStateCalled;

		public ViewHolder(@NonNull ItemListSettingBinding binding) {
			super(binding.getRoot());

			this.binding = binding;
			this.context = getContext(binding);

			binding.toggle.setOnCheckedChangeListener(this);
			binding.checkbox.setOnCheckedChangeListener(this);
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

		public void updateDescription(String payload) {
			var description = resolveDescription(payload);
			setTopMargin(binding.title, dpPx(binding.title, description == null ? 3 : -5));

			if(description == null) {
				binding.description.setVisibility(View.GONE);
			} else {
				binding.description.setVisibility(View.VISIBLE);
				binding.description.setText(description);
			}
		}

		@Nullable
		private String resolveDescription(String payload) {
			var description = item.getDescription(context);
			if(description == null) return null;

			if(description.contains("${VALUE}")) {
				var value = payload != null ? payload : handler.restoreValue(item);

				var formattedValue = switch(item.getType()) {
					case INTEGER, SELECT_INTEGER -> String.valueOf(value);
					case STRING -> (String) value;

					case BOOLEAN, SCREEN_BOOLEAN -> context.getString(
							isTrue(value) ? R.string.enabled : R.string.disabled);

					case SELECT -> {
						if(item.getItems() == null) {
							yield "";
						}

						var selected = (String) value;

						var found = stream(item.getItems())
								.filter(i -> Objects.equals(i.getKey(), selected))
								.findFirst();

						yield found.isPresent() ? found.get().getTitle(context) : selected;
					}

					default -> "";
				};

				return description.replaceAll("\\$\\{VALUE\\}",
						requireNonNullElse(formattedValue, ""));
			}

			return item.getDescription(context);
		}

		public void updateExcludableState(Selection.State state) {
			if(state == null) state = Selection.State.UNSELECTED;

			int result = switch(state) {
				case SELECTED -> MaterialCheckBox.STATE_CHECKED;
				case EXCLUDED -> MaterialCheckBox.STATE_INDETERMINATE;
				case UNSELECTED -> MaterialCheckBox.STATE_UNCHECKED;
			};

			didUpdateExcludableStateCalled = true;
			binding.checkbox.setCheckedState(result);
			didUpdateExcludableStateCalled = false;
		}

		public void bind(@NonNull SettingsItem setting) {
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
					binding.secondAction.setVisibility(View.GONE);
					return;
				}

				if(items.size() > 2) {
					binding.secondAction.setVisibility(View.GONE);
					binding.options.setImageResource(R.drawable.ic_round_dots_vertical_24);

					binding.options.setOnClickListener(v -> {
						var menu = new PopupMenu(context, binding.options);

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

					if(items.size() == 2) {
						var secondAction = items.get(1);
						binding.secondAction.setImageDrawable(secondAction.getIcon(context));
						binding.secondAction.setOnClickListener(v -> secondAction.onClick(context));
						binding.secondAction.setVisibility(View.VISIBLE);
					}
				}

				binding.options.setVisibility(View.VISIBLE);
			});

			if(isUrlValid(setting.getRawIcon())) {
				binding.icon.setVisibility(View.VISIBLE);
				clearImageTint(binding.icon);

				Glide.with(getAnyContext())
						.load(setting.getRawIcon())
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(binding.icon);
			} else {
				var icon = setting.getIcon(context);

				if(icon == null) {
					binding.icon.setVisibility(View.GONE);
				} else {
					binding.icon.setVisibility(View.VISIBLE);
					binding.icon.setImageDrawable(icon);
					setScale(binding.icon, setting.getIconSize());

					if(setting.tintIcon()) {
						setImageTintAttr(binding.icon, com.google.android.material.R.attr.colorOnSecondaryContainer);
					} else {
						clearImageTint(binding.icon);
					}
				}
			}

			updateDescription(null);

			if(setting.getType() == SettingsItemType.CATEGORY) setVerticalMargin(binding.getRoot(), dpPx(binding, -8), dpPx(binding, -12));
			else if(setting.getType() == SettingsItemType.DIVIDER) setVerticalMargin(binding.getRoot(), dpPx(binding, -12));
			else setVerticalMargin(binding.getRoot(), 0, dpPx(binding, 6));

			binding.getRoot().setMinimumHeight(dpPx(binding, isSmall ? 0 : 54));
			binding.getRoot().setClickable(!isSmall);
			binding.getRoot().setFocusable(!isSmall);

			binding.divider.setVisibility(setting.getType() == SettingsItemType.DIVIDER ? View.VISIBLE : View.GONE);

			if(setting.getType() == SettingsItemType.EXCLUDABLE) {
				binding.checkbox.setVisibility(View.VISIBLE);
				updateExcludableState(setting.getExcludableValue());
			} else {
				binding.checkbox.setVisibility(View.GONE);
			}

			if(setting.getType() == SettingsItemType.BOOLEAN || setting.getType() == SettingsItemType.SCREEN_BOOLEAN) {
				binding.toggle.setVisibility(View.VISIBLE);

				checkboxListenersActive = false;
				binding.toggle.setChecked(requireNonNullElse(setting.getBooleanValue(), false));
				checkboxListenersActive = true;
			} else {
				binding.toggle.setVisibility(View.GONE);
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(item == null || !checkboxListenersActive) return;

			switch(item.getType()) {
				case BOOLEAN, SCREEN_BOOLEAN -> {
					item.setValue(isChecked);
					handler.saveValue(item, isChecked);
					updateDescription(String.valueOf(isChecked));

					if(item.isRestartRequired()) {
						suggestToRestart(context);
					}
				}

				case EXCLUDABLE -> {
					var newValue = Objects.requireNonNullElse(
							item.getExcludableValue(), Selection.State.UNSELECTED).next();

					item.setValue(newValue);
					handler.saveValue(item, newValue);

					if(!didUpdateExcludableStateCalled) {
						updateExcludableState(newValue);
					}

					updateDescription(String.valueOf(newValue));

					if(item.isRestartRequired()) {
						suggestToRestart(context);
					}
				}
			}
		}
	}
}