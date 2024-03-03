package com.mrboomdev.awery.util.ui.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SimpleAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {
	private final OnCreateViewHolder<V> onCreateViewHolder;
	private final OnBindViewHolder<T, V> onBindViewHolder;
	private List<T> items;

	public SimpleAdapter(OnCreateViewHolder<V> onCreateViewHolder, OnBindViewHolder<T, V> onBindViewHolder, List<T> items) {
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

	public void setItems(List<T> items) {
		this.items = items;
	}

	public List<T> getItems() {
		return items;
	}

	public T getItem(int position) {
		return items.get(position);
	}

	public void addItem(T item) {
		items.add(item);
		notifyItemInserted(items.size() - 1);
	}

	public void removeItem(int position) {
		items.remove(position);
		notifyItemRemoved(position);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void clearItems() {
		items.clear();
		notifyDataSetChanged();
	}

	public void addItems(List<T> items) {
		this.items.addAll(items);
		notifyItemRangeInserted(0, items.size());
	}

	public void removeItems(List<T> items) {
		this.items.removeAll(items);
		notifyItemRangeRemoved(0, items.size());
	}

	public void removeItem(T item) {
		var index = items.indexOf(item);
		items.remove(item);
		notifyItemRemoved(index);
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