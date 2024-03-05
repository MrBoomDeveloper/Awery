package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Objects;

import com.mrboomdev.awery.databinding.ItemListSettingBinding;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private DataHandler handler;
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
			var item = holder.getItem();
			if(item == null) return;

			switch(item.getType()) {
				case BOOLEAN -> binding.toggle.performClick();
				case SCREEN -> handler.onScreenLaunchRequest(item);

				case SELECT -> new MaterialAlertDialogBuilder(parent.getContext())
						.setTitle(item.getTitle(parent.getContext()))
						.setPositiveButton("Done", (_dialog, _button) -> {

						})
						.show();
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