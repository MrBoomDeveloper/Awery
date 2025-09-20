package com.mrboomdev.awery.util.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class DropdownAdapter<T> extends BaseAdapter implements Filterable {
	private final Object lock = new Object();
	private CustomFilter filter;
	private List<T> items, filteredItems;

	public DropdownAdapter(Collection<T> items) {
		this.items = new ArrayList<>(items);
	}

	public DropdownAdapter() {
		this.items = Collections.emptyList();
	}

	@NonNull
	@Override
	public final View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		var item = getItem(position);

		if(convertView == null) {
			convertView = onCreateView(parent, getItemViewType(position));
		}

		onBindItem(item, convertView);
		return convertView;
	}

	public abstract View onCreateView(ViewGroup parent, int viewType);

	public abstract void onBindItem(T item, View view);

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

	private class CustomFilter extends Filter {

		@NonNull
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			var results = new FilterResults();

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
				return results;
			}

			var prefixString = prefix.toString().toLowerCase();
			var newValues = findResults(prefixString);

			results.values = newValues;
			results.count = newValues.size();
			return results;
		}

		private @NonNull List<Object> findResults(String prefixString) {
			final ArrayList<T> values;
			synchronized(lock) {
				values = new ArrayList<>(filteredItems);
			}

			var count = values.size();
			var newValues = new ArrayList<>();

			for(int i = 0; i < count; i++) {
				var value = values.get(i);
				var valueText = value.toString().toLowerCase();

				// First match against the whole, non-splitted value
				if(valueText.startsWith(prefixString)) {
					newValues.add(value);
				} else {
					var words = valueText.split(" ");

					for(var word : words) {
						if(word.startsWith(prefixString)) {
							newValues.add(value);
							break;
						}
					}
				}
			}

			return newValues;
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