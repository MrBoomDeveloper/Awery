package com.mrboomdev.awery.util;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

public abstract class SingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private boolean isEnabled = true;

	@NonNull
	@Contract("_ -> new")
	public static SingleViewAdapter fromView(@NonNull View view) {
		return new SingleViewAdapter() {
			@NonNull
			@Override
			public View onCreateView(@NonNull ViewGroup parent) {
				return view;
			}
		};
	}

	public void setEnabled(boolean isEnabled) {
		if(this.isEnabled && !isEnabled) {
			notifyItemRemoved(0);
		}

		if(!this.isEnabled && isEnabled) {
			notifyItemInserted(0);
		}

		this.isEnabled = isEnabled;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	protected abstract View onCreateView(ViewGroup parent);

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new RecyclerView.ViewHolder(onCreateView(parent)) {};
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

	@Override
	public int getItemCount() {
		return isEnabled ? 1 : 0;
	}
}