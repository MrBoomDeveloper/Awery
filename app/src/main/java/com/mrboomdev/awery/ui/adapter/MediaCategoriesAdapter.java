package com.mrboomdev.awery.ui.adapter;

import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.databinding.ItemListMediaCategoryBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<MediaCategoriesAdapter.ViewHolder> {
	private static final RecyclerView.RecycledViewPool itemsPool = new RecyclerView.RecycledViewPool();
	private List<Category> categories = new ArrayList<>();

	@SuppressLint("NotifyDataSetChanged")
	public MediaCategoriesAdapter() {
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

		ViewUtil.setOnApplyUiInsetsListener(binding.mediaCatalogCategoryTitle, insets -> {
			setHorizontalPadding(binding.mediaCatalogCategoryTitle, insets.left + dpPx(16));
			return true;
		}, parent);

		ViewUtil.setOnApplyUiInsetsListener(binding.mediaCatalogCategoryItems, insets -> {
			setHorizontalPadding(binding.mediaCatalogCategoryItems, insets.left + dpPx(16));
			return true;
		}, parent);

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
		private List<? extends CatalogMedia> items;
		public final String title;
		public long id;
		private ViewHolder associatedViewHolder;

		private void createListIfNecessary() {
			if(items == null) {
				items = new ArrayList<>();
			}
		}

		@SuppressLint("NotifyDataSetChanged")
		public void setItems(Collection<? extends CatalogMedia> items) {
			if(items instanceof List<? extends CatalogMedia> list) {
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

		public Category(String title, Collection<? extends CatalogMedia> items) {
			this.title = title;
			setItems(items);
		}

		public Category(String title) {
			this(title, null);
		}
	}
}