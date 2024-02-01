package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;
import com.mrboomdev.awery.util.ViewUtil;

import java.util.Collection;

import ani.awery.databinding.MediaCatalogCategoryBinding;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<MediaCategoriesAdapter.ViewHolder> {
	private static RecyclerView.RecycledViewPool itemsPool = new RecyclerView.RecycledViewPool();
	private ObservableList<Category> categories = new ObservableArrayList<>();

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

	@SuppressLint("NotifyDataSetChanged")
	public void setCategories(ObservableList<Category> categories) {
		this.categories = categories;
		notifyDataSetChanged();
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

		ViewUtil.setOnApplyUiInsetsListener(binding.mediaCatalogCategoryTitle, (view, insets) ->
				ViewUtil.setHorizontalPadding(view, insets.left + ViewUtil.dpPx(16)), parent.getRootWindowInsets());

		ViewUtil.setOnApplyUiInsetsListener(binding.mediaCatalogCategoryItems, (view, insets) ->
				ViewUtil.setHorizontalPadding(view, insets.left + ViewUtil.dpPx(16)), parent.getRootWindowInsets());

		return new ViewHolder(binding, adapter);
	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		holder.unbind();
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
		private Category associatedCategory;

		public ViewHolder(@NonNull MediaCatalogCategoryBinding binding, MediaCatalogAdapter adapter) {
			super(binding.getRoot());
			this.binding = binding;
			this.adapter = adapter;
		}

		public void bind(@NonNull Category category) {
			binding.mediaCatalogCategoryTitle.setText(category.title);
			adapter.setItems(category.items);

			this.associatedCategory = category;
			category.setAssociatedViewHolder(this);
		}

		public void unbind() {
			adapter.setItems(null);

			if(associatedCategory != null) {
				associatedCategory.setAssociatedViewHolder(null);
			}
		}
	}

	public static class Category {
		private final ObservableList<CatalogMedia<?>> items = new ObservableArrayList<>();
		public final String title;
		private ViewHolder associatedViewHolder;

		@SuppressLint("NotifyDataSetChanged")
		public void setItems(Collection<CatalogMedia<?>> items) {
			this.items.clear(false);
			this.items.addAll(items, false);

			if(associatedViewHolder != null) {
				associatedViewHolder.bind(this);
			}
		}

		protected void setAssociatedViewHolder(ViewHolder holder) {
			this.associatedViewHolder = holder;
		}

		public Category(String title) {
			this.title = title;
		}
	}
}