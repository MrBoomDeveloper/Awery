package com.mrboomdev.awery.ui.adapter;

import static com.mrboomdev.awery.app.AweryApp.getNavigationStyle;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.ItemListMediaCategoryBinding;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.ui.activity.search.SearchActivity;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<MediaCategoriesAdapter.ViewHolder> {
	private static final RecyclerView.RecycledViewPool itemsPool = new RecyclerView.RecycledViewPool();
	private final WeakHashMap<Category, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private final List<Category> categories = new ArrayList<>();

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

	@SuppressLint("NotifyDataSetChanged")
	public void addCategory(Category category) {
		this.categories.add(category);
		this.ids.put(category, idGenerator.getLong());

		// Try fixing auto scrolling to bottom
		/*if(categories.size() <= 1) notifyDataSetChanged();
		else */notifyItemInserted(categories.size() - 1);
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

		binding.header.setOnClickListener(v -> binding.expand.performClick());
		binding.recycler.setRecycledViewPool(itemsPool);
		binding.recycler.setAdapter(adapter);

		setOnApplyUiInsetsListener(binding.title, insets -> {
			if(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL && isLandscape()) {
				setLeftMargin(binding.title, insets.left + dpPx(16));
			} else {
				setLeftMargin(binding.title, dpPx(16));
			}

			return true;
		}, parent);

		setOnApplyUiInsetsListener(binding.expand, insets -> {
			setRightMargin(binding.expand, insets.right + dpPx(16));
			return true;
		}, parent);

		setOnApplyUiInsetsListener(binding.recycler, insets -> {
			if(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL && isLandscape()) {
				setHorizontalPadding(binding.recycler, insets.left + dpPx(16), insets.right + dpPx(16));
			} else {
				setHorizontalPadding(binding.recycler, dpPx(16));
			}

			return false;
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
			binding.title.setText(category.sourceFeed.title);
			adapter.setItems(category.getItems());

			this.associatedCategory = category;
			category.setAssociatedViewHolder(this);

			if(category.getItems() instanceof CatalogSearchResults<?> searchResults && searchResults.hasNextPage()) {
				binding.expand.setVisibility(View.VISIBLE);
				binding.header.setClickable(true);

				binding.expand.setOnClickListener(v -> {
					var intent = new Intent(getContext(v), SearchActivity.class);
					intent.putExtra(SearchActivity.EXTRA_GLOBAL_PROVIDER_ID, category.sourceFeed.getProivderGlobalId());
					intent.putExtra(SearchActivity.EXTRA_FILTERS, (Serializable) category.sourceFeed.filters);
					intent.putExtra(SearchActivity.EXTRA_LOADED_MEDIA, (Serializable) category.items);
					getContext(v).startActivity(intent);
				});
			} else {
				binding.header.setClickable(false);
				binding.expand.setVisibility(View.GONE);
			}

			if(category.getItems() == null || category.getItems().isEmpty()) {
				if(category.throwable != null) {
					binding.errorMessage.setText(ExceptionDescriptor.print(
							ExceptionDescriptor.unwrap(category.throwable), getContext(binding)));
				} else {
					binding.errorMessage.setText(getContext(binding).getString(R.string.nothing_found));
				}

				binding.errorMessage.setVisibility(View.VISIBLE);
				binding.recycler.setVisibility(View.GONE);
			} else {
				binding.errorMessage.setVisibility(View.GONE);
				binding.recycler.setVisibility(View.VISIBLE);
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
		public final CatalogFeed sourceFeed;
		private final CatalogSearchResults<? extends CatalogMedia> items;
		private final Throwable throwable;
		private ViewHolder associatedViewHolder;

		public List<? extends CatalogMedia> getItems() {
			return items;
		}

		protected void setAssociatedViewHolder(ViewHolder holder) {
			this.associatedViewHolder = holder;
		}

		public Category(
				@NonNull CatalogFeed sourceFeed,
				CatalogSearchResults<? extends CatalogMedia> items,
				Throwable throwable
		) {
			this.sourceFeed = sourceFeed;
			this.items = items;
			this.throwable = throwable;

			if(associatedViewHolder != null) {
				associatedViewHolder.bind(this);
			}
		}

		public Category(@NonNull CatalogFeed sourceFeed, CatalogSearchResults<? extends CatalogMedia> items) {
			this(sourceFeed, items, null);
		}

		@Contract(pure = true)
		public Category(@NonNull CatalogFeed sourceFeed, Throwable throwable) {
			this(sourceFeed, CatalogSearchResults.empty(), throwable);
		}
	}
}