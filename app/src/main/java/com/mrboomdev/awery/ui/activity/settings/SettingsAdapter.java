package com.mrboomdev.awery.ui.activity.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.databinding.ItemListSettingBinding;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private final DataHandler handler;
	private SettingsItem data;

	public SettingsAdapter(SettingsItem data, DataHandler handler) {
		this.data = data;
		this.handler = handler;
	}

	public interface DataHandler {
		void onScreenLaunchRequest(SettingsItem item);
		void save(SettingsItem item, Object newValue);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setData(@NonNull SettingsItem data) {
		this.data = data;
		notifyDataSetChanged();
	}

	private void createRadioButtons(
			ViewGroup parent,
			@NonNull List<SettingsData.SelectionItem> items,
			AtomicReference<SettingsData.SelectionItem> selectedItem
	) {
		var isChecking = new AtomicBoolean();

		for(var item : items) {
			var radio = new MaterialRadioButton(parent.getContext());
			radio.setText(item.getTitle());
			radio.setChecked(item.isSelected());

			radio.setOnCheckedChangeListener((v, isChecked) -> {
				if(isChecking.getAndSet(true)) return;
				selectedItem.set(item);

				for(int i = 0; i < parent.getChildCount(); i++) {
					var child = parent.getChildAt(i);
					if(child == radio) continue;

					if(child instanceof MaterialRadioButton materialRadio) {
						materialRadio.setChecked(false);
					} else {
						throw new IllegalStateException("Unexpected child type: " + child);
					}
				}

				item.setSelected(isChecked);
				isChecking.set(false);
			});

			parent.addView(radio, ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
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
				case SCREEN -> handler.onScreenLaunchRequest(setting);
				case ACTION -> SettingsActions.run(setting.getFullKey());

				case SELECT -> {
					final var selectionItems = new AtomicReference<Set<SettingsData.SelectionItem>>();
					final var selectedItem = new AtomicReference<SettingsData.SelectionItem>();

					var contentView = new LinearLayoutCompat(parent.getContext());
					contentView.setOrientation(LinearLayoutCompat.VERTICAL);

					var radioGroup = new RadioGroup(parent.getContext());
					radioGroup.setOrientation(LinearLayout.VERTICAL);

					var dialog = new DialogBuilder(parent.getContext())
							.setTitle(setting.getTitle(parent.getContext()))
							.addView(contentView)
							.setCancelButton("Cancel", DialogBuilder::dismiss)
							.setPositiveButton("Save", _dialog -> {
								if(radioGroup.getParent() == null) return;

								var items = selectionItems.get();
								if(items == null) return;

								if(setting.getBehaviour() != null) {
									SettingsData.saveSelectionList(setting.getBehaviour(), items);
								} else {
									if(selectedItem.get() == null) {
										_dialog.dismiss();
										return;
									}

									var prefs = AwerySettings.getInstance();
									prefs.setString(setting.getFullKey(), selectedItem.get().getId());
									prefs.saveAsync();
								}

								_dialog.dismiss();
							})
							.show();

					if(setting.getBehaviour() != null) {
						SettingsData.getSelectionList(setting.getBehaviour(), (items, e) -> {
							selectionItems.set(items);
							if(!dialog.isShown()) return;

							if(e != null) {
								dialog.dismiss();
								AweryApp.toast(e.getMessage());
								return;
							}

							var sorted = new ArrayList<>(items).stream()
									.sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
									.toList();

							AweryApp.runOnUiThread(() -> {
								createRadioButtons(radioGroup, sorted, selectedItem);
								contentView.addView(radioGroup);
							});
						});
					} else if(setting.getItems() != null && !setting.getItems().isEmpty()) {
						var selected = AwerySettings.getInstance().getString(setting.getFullKey());

						var options = new HashSet<SettingsData.SelectionItem>();
						selectionItems.set(options);

						createRadioButtons(radioGroup, setting.getItems().stream()
								.map(settingItem -> new SettingsData.SelectionItem(
										settingItem.getKey(),
										settingItem.getTitle(parent.getContext()),
										settingItem.getKey().equals(selected)))
								.toList(), selectedItem);

						contentView.addView(radioGroup);
					} else {
						throw new IllegalArgumentException("Failed to load items list");
					}
				}

				case MULTISELECT -> {
					var selectionItems = new AtomicReference<Set<SettingsData.SelectionItem>>();

					var contentView = new LinearLayoutCompat(parent.getContext());
					contentView.setOrientation(LinearLayoutCompat.VERTICAL);

					var chips = new ChipGroup(parent.getContext());
					chips.setChipSpacingVertical(ViewUtil.dpPx(-4));

					var chipsParams = new LinearLayoutCompat.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT);
					chips.setLayoutParams(chipsParams);

					var dialog = new DialogBuilder(parent.getContext())
							.setTitle(setting.getTitle(parent.getContext()))
							.addView(contentView)
							.setCancelButton("Cancel", DialogBuilder::dismiss)
							.setPositiveButton("Save", _dialog -> {
								if(chips.getParent() == null) return;

								var items = selectionItems.get();
								if(items == null) return;

								SettingsData.saveSelectionList(setting.getBehaviour(), items);
								_dialog.dismiss();
							})
							.show();

					SettingsData.getSelectionList(setting.getBehaviour(), (items, e) -> {
						selectionItems.set(items);
						if(!dialog.isShown()) return;

						if(e != null) {
							dialog.dismiss();
							AweryApp.toast(e.getMessage());
							return;
						}

						var sorted = new ArrayList<>(items).stream().sorted((a, b) ->
								a.getTitle().compareToIgnoreCase(b.getTitle()))
								.collect(Collectors.toList());

						AweryApp.runOnUiThread(() -> {
							for(var item : sorted) {
								var style = com.google.android.material.R.style.Widget_Material3_Chip_Filter;
								var context = new ContextThemeWrapper(parent.getContext(), style);

								var chip = new Chip(context);
								chip.setCheckable(true);
								chip.setText(item.getTitle());
								chip.setChecked(item.isSelected());

								chip.setOnCheckedChangeListener((_view, isChecked) ->
										item.setSelected(isChecked));

								chips.addView(chip);
							}

							contentView.addView(chips);
						});
					});
				}
			}
		});

		binding.toggle.setOnCheckedChangeListener((view, isChecked) -> {
			var item = holder.getItem();
			if(item == null || !holder.didInit()) return;

			item.setBooleanValue(isChecked);
			handler.save(item, isChecked);

			if(item.isRestartRequired()) {
				suggestToRestart(parent);
			}
		});

		return holder;
	}

	private void suggestToRestart(@NonNull View parentView) {
		Snackbar.make(parentView, "Restart is required to apply changes", 2250)
				.setAction("Restart", _view -> {
					var context = parentView.getContext();
					var pm = context.getPackageManager();

					var intent = pm.getLaunchIntentForPackage(context.getPackageName());
					var component = Objects.requireNonNull(intent).getComponent();

					var mainIntent = Intent.makeRestartActivityTask(component);
					mainIntent.setPackage(context.getPackageName());
					context.startActivity(mainIntent);

					Runtime.getRuntime().exit(0);
				})
				.show();
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(data.getItems().get(position));
	}

	@Override
	public int getItemCount() {
		return data.getItems().size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemListSettingBinding binding;
		private final Context context;
		private SettingsItem item;
		private boolean didInit;

		public ViewHolder(@NonNull ItemListSettingBinding binding) {
			super(binding.getRoot());

			this.binding = binding;
			this.context = binding.getRoot().getContext();
		}

		public SettingsItem getItem() {
			return item;
		}

		public boolean didInit() {
			return didInit;
		}

		public void bind(@NonNull SettingsItem item) {
			this.didInit = false;
			this.item = item;

			binding.getRoot().setVisibility(item.isVisible() ? View.VISIBLE : View.GONE);

			binding.toggle.setVisibility(View.GONE);
			binding.title.setText(item.getTitle(context));

			var description = item.getDescription(context);
			if(description == null) {
				binding.description.setVisibility(View.GONE);
			} else {
				binding.description.setVisibility(View.VISIBLE);
				binding.description.setText(description);
			}

			var icon = item.getIcon(context);
			if(icon == null) {
				binding.icon.setVisibility(View.GONE);
			} else {
				binding.icon.setVisibility(View.VISIBLE);
				binding.icon.setImageDrawable(icon);
			}

			switch(item.getType()) {
				case BOOLEAN -> {
					binding.toggle.setVisibility(View.VISIBLE);
					binding.toggle.setChecked(item.getBooleanValue());
				}
			}

			this.didInit = true;
		}
	}
}