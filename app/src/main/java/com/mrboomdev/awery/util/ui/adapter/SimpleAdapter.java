package com.mrboomdev.awery.util.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SimpleAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {
	private final OnCreateViewHolder<V> onCreateViewHolder;
	private final OnBindViewHolder<T, V> onBindViewHolder;
	private List<T> items;

	public SimpleAdapter(
			OnCreateViewHolder<V> onCreateViewHolder,
			OnBindViewHolder<T, V> onBindViewHolder,
			List<T> items,
			boolean useStableIds
	) {
		this.items = items;
		this.onCreateViewHolder = onCreateViewHolder;
		this.onBindViewHolder = onBindViewHolder;
		super.setHasStableIds(useStableIds);
	}

	public SimpleAdapter(
			OnCreateViewHolder<V> onCreateViewHolder,
			OnBindViewHolder<T, V> onBindViewHolder,
			List<T> items
	) {
		this.items = items;
		this.onCreateViewHolder = onCreateViewHolder;
		this.onBindViewHolder = onBindViewHolder;
	}

	public SimpleAdapter(OnCreateViewHolder<V> onCreateViewHolder, List<T> items) {
		this.items = items;
		this.onCreateViewHolder = onCreateViewHolder;
		this.onBindViewHolder = (a, b) -> {};
	}
	public SimpleAdapter(OnCreateViewHolder<V> onCreateViewHolder) {
		this.items = new ArrayList<>();
		this.onCreateViewHolder = onCreateViewHolder;
		this.onBindViewHolder = (a, b) -> {};
	}

	public SimpleAdapter(OnCreateViewHolder<V> onCreateViewHolder, OnBindViewHolder<T, V> onBindViewHolder) {
		this.items = new ArrayList<>();
		this.onCreateViewHolder = onCreateViewHolder;
		this.onBindViewHolder = onBindViewHolder;
	}

	@Override
	public void setHasStableIds(boolean hasStableIds) {
		throw new UnsupportedOperationException("You can't enable stable ids after the adapter has been created!");
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public List<T> getItems() {
		return items;
	}

	@NonNull
	@Override
	public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return onCreateViewHolder.onCreateViewHolder(parent);
	}

	@Override
	public void onBindViewHolder(@NonNull V holder, int position) {
		onBindViewHolder.onBindViewHolder(items.get(position), holder);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public interface OnBindViewHolder<T, V extends RecyclerView.ViewHolder> {
		void onBindViewHolder(T item, V holder);
	}

	public interface OnCreateViewHolder<V extends RecyclerView.ViewHolder> {
		V onCreateViewHolder(ViewGroup parent);
	}
}