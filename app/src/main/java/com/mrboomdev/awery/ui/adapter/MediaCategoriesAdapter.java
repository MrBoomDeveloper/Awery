package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.databinding.ItemListMediaCategoryBinding;
import com.mrboomdev.awery.util.observable.ObservableList;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<MediaCategoriesAdapter.ViewHolder> implements ObservableList.AddObserver<MediaCategoriesAdapter.Category> {
	private static RecyclerView.RecycledViewPool itemsPool = new RecyclerView.RecycledViewPool();
	private List<Category> categories = new ArrayList<>();

	@SuppressLint("NotifyDataSetChanged")
	public MediaCategoriesAdapter() {
		AweryApp.registerDisposable(() -> itemsPool = null);
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return categories.get(position).id;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setCategories(List<Category> categories) {
		this.categories = categories;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemListMediaCategoryBinding.inflate(inflater, parent, false);
		var viewHolder = new ViewHolder(binding);
		var adapter = new MediaCatalogAdapter();

		binding.mediaCatalogCategoryItems.setRecycledViewPool(itemsPool);
		binding.mediaCatalogCategoryItems.setHasFixedSize(true);
		binding.mediaCatalogCategoryItems.setAdapter(adapter);

		ViewUtil.setOnApplyUiInsetsListener(binding.mediaCatalogCategoryTitle, insets ->
				ViewUtil.setHorizontalPadding(binding.mediaCatalogCategoryTitle,
						insets.left + ViewUtil.dpPx(16)), parent.getRootWindowInsets());

		ViewUtil.setOnApplyUiInsetsListener(binding.mediaCatalogCategoryItems, insets ->
				ViewUtil.setHorizontalPadding(binding.mediaCatalogCategoryItems,
						insets.left + ViewUtil.dpPx(16)), parent.getRootWindowInsets());

		viewHolder.setAdapter(adapter);
		return viewHolder;
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

	@Override
	public void added(Category item, int index) {
		notifyItemInserted(index);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemListMediaCategoryBinding binding;
		private MediaCatalogAdapter adapter;
		private Category associatedCategory;

		public ViewHolder(@NonNull ItemListMediaCategoryBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public Category getCategory() {
			return associatedCategory;
		}

		public void setAdapter(MediaCatalogAdapter adapter) {
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
		private List<CatalogMedia> items;
		public final String title;
		public long id;
		private ViewHolder associatedViewHolder;

		private void createListIfNecessary() {
			if(items == null) {
				items = new ArrayList<>();
			}
		}

		@SuppressLint("NotifyDataSetChanged")
		public void setItems(Collection<CatalogMedia> items) {
			if(items instanceof List<CatalogMedia> list) {
				this.items = list;
			} else if(items != null) {
				this.items = new ArrayList<>(items);
			}

			if(associatedViewHolder != null) {
				createListIfNecessary();
				associatedViewHolder.bind(this);
			}
		}

		protected void setAssociatedViewHolder(ViewHolder holder) {
			this.associatedViewHolder = holder;
		}

		public Category(String title, Collection<CatalogMedia> items) {
			this.title = title;
			setItems(items);
		}

		public Category(String title) {
			this(title, null);
		}
	}
}