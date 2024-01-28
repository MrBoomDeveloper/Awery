package com.mrboomdev.awery.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import ani.awery.databinding.MediaCatalogCategoryBinding;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<MediaCategoriesAdapter.ViewHolder> {
	private static RecyclerView.RecycledViewPool itemsPool = new RecyclerView.RecycledViewPool();
	private final ObservableList<Category> categories = new ObservableArrayList<>();

	public MediaCategoriesAdapter() {
		AweryApp.registerDisposable(() -> itemsPool = null);
		categories.observeAdditions((category, index) -> notifyItemInserted(index));
		categories.observeChanges((_new, _old, index) -> notifyItemChanged(index));
		categories.observeRemovals((category, index) -> notifyItemRemoved(index));
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void clearCategories() {
		categories.clear();
	}

	public void addCategory(Category category, boolean notify) {
		categories.add(category, notify);
	}

	public void addCategory(Category category) {
		categories.add(category);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = MediaCatalogCategoryBinding.inflate(inflater, parent, false);
		var adapter = new MediaCatalogAdapter();

		binding.mediaCatalogCategoryItems.setRecycledViewPool(itemsPool);
		binding.mediaCatalogCategoryItems.setHasFixedSize(true);
		binding.mediaCatalogCategoryItems.setAdapter(adapter);

		return new ViewHolder(binding, adapter);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(categories.get(position));
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogCategoryBinding binding;
		private final MediaCatalogAdapter adapter;

		public ViewHolder(@NonNull MediaCatalogCategoryBinding binding, MediaCatalogAdapter adapter) {
			super(binding.getRoot());
			this.binding = binding;
			this.adapter = adapter;
		}

		public void bind(@NonNull Category category) {
			binding.mediaCatalogCategoryTitle.setText(category.title);
			adapter.setItems(category.items);
		}
	}

	public static class Category {
		public final ObservableList<String> items = new ObservableArrayList<>();
		public final String title;

		public Category(String title) {
			this.title = title;
		}
	}
}