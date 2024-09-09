package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.App.snackbar;
import static com.mrboomdev.awery.app.Lifecycle.getActivity;
import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static com.mrboomdev.awery.app.Lifecycle.getContext;
import static com.mrboomdev.awery.app.Lifecycle.getFragmentManager;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.isTrue;
import static com.mrboomdev.awery.util.NiceUtils.isUrlValid;
import static com.mrboomdev.awery.util.NiceUtils.nonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.NiceUtils.with;
import static com.mrboomdev.awery.util.ui.ViewUtil.clearImageTint;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setImageTintAttr;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalMargin;
import static java.util.Objects.requireNonNullElse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.VectorDrawable;
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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.Lifecycle;
import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.app.data.settings.base.AndroidSetting;
import com.mrboomdev.awery.app.data.settings.base.ParsedSetting;
import com.mrboomdev.awery.databinding.ItemListSettingBinding;
import com.mrboomdev.awery.ext.data.Selection;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.ext.data.Settings;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.Lazy;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.SelectionDialog;
import com.mrboomdev.awery.util.ui.fields.EditTextField;
import com.mrboomdev.awery.util.ui.fields.FancyField;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private final WeakHashMap<Setting, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private final ScreenLauncher screenLauncher;
	private List<Setting> items;
	private Setting screen;

	public interface ScreenLauncher {
		void launchScreen(Setting setting);
	}

	private static final Lazy.Basic<DateFormat> DATE_FORMAT = Lazy.createBasic(() ->
			android.text.format.DateFormat.getMediumDateFormat(getAnyContext()));

	public SettingsAdapter(@NotNull Setting screen, @NotNull ScreenLauncher screenLauncher) {
		setHasStableIds(true);
		setScreen(screen, false);
		this.screenLauncher = screenLauncher;
	}

	public SettingsAdapter(@NotNull ScreenLauncher screenLauncher) {
		this(new Setting.Builder().setItems().build(), screenLauncher);
	}

	public Setting getScreen() {
		return screen;
	}

	public void onEmptyStateChanged(boolean isEmpty) {}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(@NonNull Settings items, boolean notify) {
		this.screen = null;

		setScreen(new Setting(Setting.Type.SCREEN) {
			@Override
			public Settings getItems() {
				return items;
			}

			@Override
			public void addSettingsObserver(Observer observer) {
				// Do nothing because this screen will never get updated
			}
		}, notify);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setScreen(@NonNull Setting screen, boolean notify) {
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
				.filter(setting -> !(setting instanceof ParsedSetting androidSetting) || androidSetting.isVisible())
				.toList());

		for(var item : items) {
			var id = idGenerator.getLong();
			ids.put(item, id);
			item.setParent(screen);
		}

		screen.addSettingsObserver(new Setting.Observer() {
			@Override
			public void onSettingAddition(Setting item, int position) {
				item.setParent(screen);

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
			public void onSettingRemoval(Setting item) {
				item.setParent(null);

				var index = items.indexOf(item);
				items.remove(item);
				notifyItemRemoved(index);

				if(items.isEmpty()) {
					onEmptyStateChanged(true);
				}
			}

			@Override
			public void onSettingChange(Setting newItem, Setting oldItem) {
				oldItem.setParent(null);
				newItem.setParent(screen);

				var index = items.indexOf(oldItem);

				if(newItem != oldItem) {
					items.set(index, newItem);
					ids.put(newItem, ids.get(oldItem));
				}

				notifyItemChanged(index);
			}
		});

		if(notify) {
			notifyDataSetChanged();
		}
	}

	public List<Setting> getItems() {
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

	public void setScreen(@NonNull Setting data) {
		setScreen(data, true);
	}

	@NonNull
	@Override
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
			if(setting == null || setting.getType() == null) return;

			switch(setting.getType()) {
				case BOOLEAN -> binding.toggle.performClick();
				case EXCLUDABLE -> binding.checkbox.performClick();
				case SCREEN, SCREEN_BOOLEAN -> screenLauncher.launchScreen(setting);

				case ACTION -> {
					setting.onClick();
					holder.updateDescription(null);

					if(setting instanceof ParsedSetting androidSetting && androidSetting.isRestartRequired()) {
						suggestToRestart(context);
					}
				}

				case STRING -> {
					var inputField = new EditTextField(context);
					inputField.setImeFlags(EditorInfo.IME_ACTION_DONE);
					inputField.setText(setting.getValue());
					inputField.setLinesCount(1);

					var dialog = new DialogBuilder(context)
							.setTitle(setting.getTitle())
							.setMessage(setting.getDescription())
							.addView(inputField.getView())
							.setNegativeButton(context.getString(R.string.cancel), DialogBuilder::dismiss)
							.setPositiveButton(R.string.ok, _dialog -> {
								setting.setValue(inputField.getText());

								if(setting instanceof ParsedSetting androidSetting && androidSetting.isRestartRequired()) {
									suggestToRestart(context);
								}

								_dialog.dismiss();
							})
							.show();

					inputField.setCompletionListener(dialog::performPositiveClick);
				}

				case INTEGER -> {
					Object field;

					if(setting.getFrom() != null && setting.getTo() != null) {
						var slider = new Slider(context);
						slider.setValueFrom(setting.getFrom());
						slider.setValueTo(setting.getTo());
						slider.setStepSize(1);
						slider.setValue(requireNonNullElse((Integer) setting.getValue(), 0));
						field = slider;
					} else {
						var inputField = new EditTextField(context);
						inputField.setImeFlags(EditorInfo.IME_ACTION_DONE);
						inputField.setType(EditorInfo.TYPE_CLASS_NUMBER);
						inputField.setText(setting.getValue());
						inputField.setLinesCount(1);
						field = inputField;
					}

					var dialog = new DialogBuilder(context)
							.setTitle(setting.getTitle())
							.setMessage(setting.getDescription())
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

										if(setting.getFrom() != null && setting.getTo() != null) {
											if(number > setting.getTo()) {
												inputField.setError("Value is too high. Max is: " + Math.round(setting.getTo()));
												return;
											}

											if(number < setting.getFrom()) {
												inputField.setError("Value is too low. Min is: " + Math.round(setting.getFrom()));
												return;
											}
										}

										setting.setValue(number);
									} catch(NumberFormatException e) {
										inputField.setError(R.string.this_not_number);
										return;
									}
								} else {
									var slider = (Slider) field;
									setting.setValue((int) slider.getValue());
								}

								_dialog.dismiss();

								if(setting instanceof AndroidSetting androidSetting
										&& androidSetting.isRestartRequired()) {
									suggestToRestart(context);
								}
							})
							.show();

					if(field instanceof EditTextField inputField) {
						inputField.setCompletionListener(dialog::performPositiveClick);
					}
				}

				case DATE -> {
					var dialogBuilder = MaterialDatePicker.Builder.datePicker();

					if(setting.getValue() instanceof Long oldDate) {
						dialogBuilder.setSelection(oldDate);
					}

					var dialog = dialogBuilder.build();
					dialog.addOnPositiveButtonClickListener(setting::setValue);
					dialog.show(getFragmentManager(context), "date_picker");
				}

				case SELECT, SELECT_INTEGER -> {
					var dialog = new SelectionDialog.Single<Selection.Selectable<String>>(context)
							.setTitle(setting.getTitle())
							.setNegativeButton(R.string.cancel, SelectionDialog::dismiss)
							.setPositiveButton(R.string.confirm, (_dialog, item) -> {
								if(item == null) {
									_dialog.dismiss();
									return;
								}

								var id = item.getId();
								holder.updateDescription(id);

								if(setting.getType() == Setting.Type.SELECT_INTEGER) {
									setting.setValue(Integer.parseInt(id));
								} else {
									setting.setValue(id);
								}

								_dialog.dismiss();

								if(setting instanceof AndroidSetting androidSetting
										&& androidSetting.isRestartRequired()) {
									suggestToRestart(context);
								}
							}).show();

					if(setting.getExtra() != null) {
						/*SettingsData.getSelectionList(context, setting.getExtra(), (items, e) -> {
							if(!dialog.isShown()) return;

							if(e != null) {
								dialog.dismiss();
								toast(e.getMessage());
								return;
							}

							runOnUiThread(() -> dialog.setItems(items));
						});*/

						throw new UnsupportedOperationException("Stub!");
					} else if(setting.getItems() != null) {
						var selected = (setting.getType() == Setting.Type.SELECT_INTEGER)
								? String.valueOf(setting.getIntegerValue()) : setting.getStringValue();

						dialog.setItems(setting.getItems().stream()
								.filter(item -> !(item instanceof AndroidSetting androidSetting) || androidSetting.isVisible())
								.map(item -> new Selection.Selectable<>(
										item.getTitle(),
										item.getKey(),
										Objects.equals(item.getKey(), selected) ? Selection.State.SELECTED : Selection.State.UNSELECTED
								)).collect(Selection.collect()));
					} else {
						throw new IllegalArgumentException("Failed to load items list");
					}
				}

				case MULTISELECT -> {
					var dialog = new SelectionDialog.Multi<Selection.Selectable<String>>(context)
							.setTitle(setting.getTitle())
							.setNegativeButton(R.string.cancel, SelectionDialog::dismiss)
							.setPositiveButton(R.string.confirm, (_dialog, selection) -> {
								setting.setValue(selection);
								_dialog.dismiss();

								if(setting instanceof AndroidSetting androidSetting
										&& androidSetting.isRestartRequired()) {
									suggestToRestart(context);
								}
							})
							.show();

					if(setting.getExtra() != null) {
						/*SettingsData.getSelectionList(context, setting.getExtra(), (items, e) -> {
							if(!dialog.isShown()) return;

							if(e != null) {
								dialog.dismiss();
								toast(e.getMessage());
								return;
							}

							runOnUiThread(() -> dialog.setItems(items));
						});*/

						throw new UnsupportedOperationException("Stub!");
					} else if(setting.getItems() != null) {
						var selected = setting.getSetValue();

						dialog.setItems(setting.getItems().stream()
								.filter(item -> !(item instanceof AndroidSetting androidSetting) || androidSetting.isVisible())
								.map(item -> new Selection.Selectable<>(
										item.getTitle(),
										item.getKey(),
										(find(selected, a -> Objects.equals(a.getKey(), item.getKey())) != null)
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
				R.string.restart_to_apply_settings, R.string.restart, Lifecycle::restartApp);
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
		private Setting item;
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

		public Setting getItem() {
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
			var description = item.getDescription();

			if(description == null || item.getType() == null) {
				return description;
			}

			if(description.contains("${VALUE}")) {
				var value = payload != null ? payload : item.getValue();

				var formattedValue = switch(item.getType()) {
					case INTEGER, SELECT_INTEGER -> String.valueOf(value);
					case STRING -> (String) value;
					case DATE -> value == null ? "" : DATE_FORMAT.get().format(value);

					case BOOLEAN, SCREEN_BOOLEAN -> context.getString(
							isTrue(value) ? R.string.enabled : R.string.disabled);

					case SELECT -> {
						if(item.getItems() == null) {
							yield "";
						}

						var selected = (String) value;

						var found = stream(item.getItems())
								.filter(i -> i != null && Objects.equals(i.getKey(), selected))
								.findFirst().orElse(null);

						yield found != null ? found.getTitle() : selected;
					}

					default -> "";
				};

				return description.replaceAll("\\$\\{VALUE\\}",
						nonNullElse(formattedValue, ""));
			}

			return item.getDescription();
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

		public void bind(@NonNull Setting setting) {
			this.item = setting;

			var isSmall = setting.getType() == Setting.Type.CATEGORY
					|| setting.getType() == Setting.Type.DIVIDER;

			with(setting.getTitle(), title -> {
				if(title == null) {
					binding.title.setText(null);
					return;
				}

				binding.title.setTextColor(resolveAttrColor(context, setting.getType() == Setting.Type.CATEGORY
						? com.google.android.material.R.attr.colorOnSecondaryContainer
						: com.google.android.material.R.attr.colorOnBackground));

				binding.title.setTextSize(TypedValue.COMPLEX_UNIT_SP,
						(setting.getType() == Setting.Type.CATEGORY) ? 14 : 16);

				binding.title.setText(title);
			});

			with(setting.getActions(), items -> {
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
							var item = items.get(i);

							if(item != null) {
								menu.getMenu().add(0, i, 0, item.getTitle());
							}
						}

						menu.setOnMenuItemClickListener(item -> {
							var theItem = items.get(item.getItemId());

							if(theItem != null) {
								theItem.onClick();
								return true;
							}

							return false;
						});

						menu.show();
					});
				} else {
					var action = items.get(0);
					binding.options.setOnClickListener(v -> action.onClick());

					if(action.getIcon() instanceof AndroidImage androidImage) {
						androidImage.applyTo(binding.options);
					} else {
						binding.options.setImageDrawable(null);
					}

					if(items.size() == 2) {
						var secondAction = items.get(1);

						if(secondAction.getIcon() instanceof AndroidImage androidImage) {
							androidImage.applyTo(binding.secondAction);
						} else {
							binding.secondAction.setImageDrawable(null);
						}

						binding.secondAction.setOnClickListener(v -> secondAction.onClick());
						binding.secondAction.setVisibility(View.VISIBLE);
					}
				}

				binding.options.setVisibility(View.VISIBLE);
			});

			if(setting.getIcon() instanceof AndroidImage androidImage && isUrlValid(androidImage.getRawRes())) {
				binding.icon.setVisibility(View.VISIBLE);
				clearImageTint(binding.icon);

				Glide.with(getAnyContext())
						.load(androidImage.getRawRes())
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(binding.icon);
			} else {
				var icon = setting.getIcon() instanceof AndroidImage androidImage ? androidImage.getDrawable() : null;

				if(icon == null) {
					binding.icon.setVisibility(View.GONE);
				} else {
					binding.icon.setVisibility(View.VISIBLE);
					binding.icon.setImageDrawable(icon);
					//setScale(binding.icon, setting.getIconSize());

					if(icon instanceof VectorDrawable) {
						setImageTintAttr(binding.icon, com.google.android.material.R.attr.colorOnSecondaryContainer);
					} else {
						clearImageTint(binding.icon);
					}
				}
			}

			updateDescription(null);

			if(setting.getType() == Setting.Type.CATEGORY) setVerticalMargin(binding.getRoot(), dpPx(binding, -8), dpPx(binding, -12));
			else if(setting.getType() == Setting.Type.DIVIDER) setVerticalMargin(binding.getRoot(), dpPx(binding, -12));
			else setVerticalMargin(binding.getRoot(), 0, dpPx(binding, 6));

			binding.getRoot().setMinimumHeight(dpPx(binding, isSmall ? 0 : 54));
			binding.getRoot().setClickable(!isSmall);
			binding.getRoot().setFocusable(!isSmall);

			binding.divider.setVisibility(setting.getType() == Setting.Type.DIVIDER ? View.VISIBLE : View.GONE);

			if(setting.getType() == Setting.Type.EXCLUDABLE) {
				binding.checkbox.setVisibility(View.VISIBLE);
				updateExcludableState(setting.getExcludableValue());
			} else {
				binding.checkbox.setVisibility(View.GONE);
			}

			if(setting.getType() == Setting.Type.BOOLEAN || setting.getType() == Setting.Type.SCREEN_BOOLEAN) {
				binding.toggle.setVisibility(View.VISIBLE);

				checkboxListenersActive = false;
				binding.toggle.setChecked(Boolean.TRUE.equals(setting.getBooleanValue()));
				checkboxListenersActive = true;
			} else {
				binding.toggle.setVisibility(View.GONE);
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(item == null || item.getType() == null || !checkboxListenersActive) return;

			switch(item.getType()) {
				case BOOLEAN, SCREEN_BOOLEAN -> {
					item.setValue(isChecked);
					updateDescription(String.valueOf(isChecked));

					if(item instanceof AndroidSetting androidSetting
							&& androidSetting.isRestartRequired()) {
						suggestToRestart(context);
					}
				}

				case EXCLUDABLE -> {
					var newValue = requireNonNullElse(
							item.getExcludableValue(), Selection.State.UNSELECTED).next();

					item.setValue(newValue);

					if(!didUpdateExcludableStateCalled) {
						updateExcludableState(newValue);
					}

					updateDescription(String.valueOf(newValue));

					if(item instanceof AndroidSetting androidSetting
							&& androidSetting.isRestartRequired()) {
						suggestToRestart(context);
					}
				}
			}
		}
	}
}