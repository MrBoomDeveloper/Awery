package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mrboomdev.awery.data.settings.SettingsFactory;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Objects;

import ani.awery.R;
import ani.awery.databinding.SettingsItemBinding;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private ScreenRequestListener screenRequestListener;
	private SettingsItem data;

	public SettingsAdapter(SettingsItem data) {
		this.data = data;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setData(SettingsItem data) {
		this.data = data;
		notifyDataSetChanged();
	}

	public void setScreenRequestListener(ScreenRequestListener listener) {
		this.screenRequestListener = listener;
	}

	public interface ScreenRequestListener {
		void request(SettingsItem item);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = SettingsItemBinding.inflate(inflater, parent, false);
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

				case SCREEN -> {
					if(screenRequestListener != null) {
						screenRequestListener.request(item);
					}
				}

				case SELECT -> new MaterialAlertDialogBuilder(parent.getContext())
						.setTitle(item.getTitle(parent.getContext()))
						.setPositiveButton("Done", (_dialog, _button) -> {

						})
						.show();
			}
		});

		binding.toggle.setOnCheckedChangeListener((view, isChecked) -> {
			var item = holder.getItem();
			if(item == null) return;

			if(item.isRestartRequired()) {
				Snackbar.make(parent, "Restart is required to apply changes", 2250)
						.setAction("Restart", _view -> {
							var context = _view.getContext();
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
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(data.getItems().get(position));
	}

	@Override
	public int getItemCount() {
		return data.getItems().size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final SettingsItemBinding binding;
		private final Context context;
		private SettingsItem item;

		public ViewHolder(@NonNull SettingsItemBinding binding) {
			super(binding.getRoot());

			this.binding = binding;
			this.context = binding.getRoot().getContext();
		}

		public SettingsItem getItem() {
			return item;
		}

		public void bind(@NonNull SettingsItem item) {
			this.item = item;

			binding.title.setText(item.getTitle(context));
			binding.description.setText(item.getDescription(context));

			var description = item.getDescription(context);
			if(description == null) {
				binding.description.setVisibility(View.GONE);
			} else {
				binding.description.setVisibility(View.VISIBLE);
				binding.description.setText(description);
			}

			binding.toggle.setVisibility(item.getType() == SettingsItemType.BOOLEAN ? View.VISIBLE : View.GONE);
		}
	}
}