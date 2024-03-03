package com.mrboomdev.awery.util.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomArrayAdapter<T> extends BaseAdapter implements Filterable {
	private final OnCreateView<T> onCreate;
	private final Object lock = new Object();
	private CustomFilter filter;
	private List<T> items, filteredItems;

	public CustomArrayAdapter(@NonNull Context context, Collection<T> items, OnCreateView<T> onCreate) {
		this.onCreate = onCreate;
		this.items = new ArrayList<>(items);
	}

	public CustomArrayAdapter(@NonNull Context context, OnCreateView<T> onCreate) {
		this(context, new ArrayList<>(), onCreate);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		var item = getItem(position);
		return onCreate.createView(item, convertView, parent);
	}

	public void setItems(Collection<T> items) {
		this.items.clear();
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	@Nullable
	@Override
	public T getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Filter getFilter() {
		if(filter == null) {
			filter = new CustomFilter();
		}

		return filter;
	}

	public interface OnCreateView<T> {
		View createView(T item, View recycledView, ViewGroup parent);
	}

	private class CustomFilter extends Filter {
		@NonNull
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			final FilterResults results = new FilterResults();

			if(filteredItems == null) {
				synchronized(lock) {
					filteredItems = new ArrayList<>(items);
				}
			}

			if(prefix == null || prefix.length() == 0) {
				final ArrayList<T> list;

				synchronized(lock) {
					list = new ArrayList<>(filteredItems);
				}

				results.values = list;
				results.count = list.size();
			} else {
				final String prefixString = prefix.toString().toLowerCase();

				final ArrayList<T> values;
				synchronized(lock) {
					values = new ArrayList<>(filteredItems);
				}

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<>();

				for (int i = 0; i < count; i++) {
					final T value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						for (String word : words) {
							if (word.startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
			items = (List<T>) results.values;

			if(results.count > 0) notifyDataSetChanged();
			else notifyDataSetInvalidated();
		}
	}
}