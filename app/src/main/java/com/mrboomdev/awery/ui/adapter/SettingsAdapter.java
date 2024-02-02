package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.data.settings.SettingsFactory;
import com.mrboomdev.awery.data.settings.SettingsItem;

import ani.awery.R;
import ani.awery.databinding.SettingsItemBinding;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private SettingsItem data;

	public SettingsAdapter(SettingsItem data) {
		this.data = data;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setData(SettingsItem data) {
		this.data = data;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = SettingsItemBinding.inflate(inflater, parent, false);
		return new ViewHolder(binding);
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
		private final SettingsItemBinding binding;
		private final Context context;

		public ViewHolder(@NonNull SettingsItemBinding binding) {
			super(binding.getRoot());

			this.binding = binding;
			this.context = binding.getRoot().getContext();
		}

		public void bind(@NonNull SettingsItem item) {
			binding.title.setText(item.getFullKey());
		}
	}

	@Nullable
	private String getSettingTitle(@NonNull Context context, String key) {
		try {
			var clazz = R.string.class;
			var field = clazz.getField(key + "_title");
			return context.getString(field.getInt(null));
		} catch(NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
}