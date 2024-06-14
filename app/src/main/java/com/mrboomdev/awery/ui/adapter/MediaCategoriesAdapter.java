package com.mrboomdev.awery.ui.adapter;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.databinding.ItemListMediaCategoryBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<MediaCategoriesAdapter.ViewHolder> {
	private static final RecyclerView.RecycledViewPool itemsPool = new RecyclerView.RecycledViewPool();
	private final WeakHashMap<Category, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private List<Category> categories = new ArrayList<>();

	@SuppressLint("NotifyDataSetChanged")
	public MediaCategoriesAdapter() {
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return Objects.requireNonNull(ids.get(categories.get(position)));
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setCategories(@NonNull List<Category> categories) {
		this.categories.clear();
		this.categories.addAll(categories);
		this.idGenerator.clear();

		for(var category : categories) {
			ids.put(category, idGenerator.getLong());
		}

		notifyDataSetChanged();
	}

	public void addCategory(Category category) {
		this.categories.add(category);
		this.ids.put(category, idGenerator.getLong());
		notifyItemInserted(categories.size() - 1);
	}

	public void removeCategory(Category category) {
		var wasIndex = this.categories.indexOf(category);
		this.categories.remove(category);
		this.ids.remove(category);
		notifyItemRemoved(wasIndex);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemListMediaCategoryBinding.inflate(inflater, parent, false);
		var viewHolder = new ViewHolder(binding);
		var adapter = new MediaCatalogAdapter();

		binding.mediaCatalogCategoryItems.setRecycledViewPool(itemsPool);
		binding.mediaCatalogCategoryItems.setAdapter(adapter);

		setOnApplyUiInsetsListener(binding.mediaCatalogCategoryTitle, insets -> {
			setHorizontalPadding(binding.mediaCatalogCategoryTitle, insets.left + dpPx(16));
			return true;
		}, parent);

		setOnApplyUiInsetsListener(binding.expand, insets -> {
			setRightMargin(binding.expand, insets.right + dpPx(10));
			return true;
		}, parent);

		setOnApplyUiInsetsListener(binding.mediaCatalogCategoryItems, insets -> {
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

		public void setAdapter(MediaCatalogAdapter adapter) {
			this.adapter = adapter;
		}

		public void bind(@NonNull Category category) {
			binding.mediaCatalogCategoryTitle.setText(category.title);
			adapter.setItems(category.items);

			this.associatedCategory = category;
			category.setAssociatedViewHolder(this);

			if(category.items instanceof CatalogSearchResults<?> searchResults && searchResults.hasNextPage()) {
				binding.expand.setVisibility(View.VISIBLE);

				binding.expand.setOnClickListener(v -> {
					toast("Sorry, but this screen isn't done yet!");
				});
			} else {
				binding.expand.setVisibility(View.GONE);
			}
		}

		public void unbind() {
			adapter.setItems(null);

			if(associatedCategory != null) {
				associatedCategory.setAssociatedViewHolder(null);
			}
		}
	}

	public static class Category {
		public final String title;
		public long id;
		private List<? extends CatalogMedia> items;
		private ViewHolder associatedViewHolder;

		@SuppressLint("NotifyDataSetChanged")
		public void setItems(Collection<? extends CatalogMedia> items) {
			if(items instanceof List<? extends CatalogMedia> list) {
				this.items = list;
			} else if(items != null) {
				this.items = new ArrayList<>(items);
			}

			if(associatedViewHolder != null) {
				if(items == null) {
					this.items = Collections.emptyList();
				}

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
	}
}