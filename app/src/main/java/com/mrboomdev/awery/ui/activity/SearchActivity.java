package com.mrboomdev.awery.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.compose.runtime.AtomicInt;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.catalog.anilist.query.AnilistSearchQuery;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.databinding.ItemGridMediaCatalogBinding;
import com.mrboomdev.awery.databinding.LayoutHeaderSearchBinding;
import com.mrboomdev.awery.databinding.LayoutLoadingBinding;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.observable.ObservableArrayList;
import com.mrboomdev.awery.util.observable.ObservableEmptyList;
import com.mrboomdev.awery.util.observable.ObservableList;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

public class SearchActivity extends AppCompatActivity {
	private static final int LOADING_VIEW_TYPE = 1;
	private static final String TAG = "SearchActivity";
	private final Adapter adapter = new Adapter();
	private final ObservableList<CatalogMedia> items = new ObservableArrayList<>();
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> loadingAdapter;
	private LayoutHeaderSearchBinding header;
	private SwipeRefreshLayout refresher;
	private RecyclerView recycler;
	/* We initially set this value to "true" so that list won't try
	to load anything because we haven't typed anything yet. */
	private boolean isLoading = true, didReachedEnd;
	private int searchId, currentPage;
	private String searchQuery;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		var linear = new LinearLayoutCompat(this);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		setContentView(linear);

		header = LayoutHeaderSearchBinding.inflate(getLayoutInflater(), linear, true);
		header.back.setOnClickListener(v -> finish());

		header.edittext.requestFocus();

		var inputManager = getSystemService(InputMethodManager.class);
		inputManager.showSoftInput(header.edittext, 0);

		header.edittext.setOnEditorActionListener((v, action, event) -> {
			if(action == EditorInfo.IME_ACTION_SEARCH) {
				this.searchQuery = v.getText().toString();
				this.didReachedEnd = false;
				search(0);
				inputManager.hideSoftInputFromWindow(header.edittext.getWindowToken(), 0);
				return true;
			}

			return false;
		});

		ViewUtil.setOnApplyUiInsetsListener(header.getRoot(), insets -> {
			ViewUtil.setTopMargin(header.getRoot(), insets.top);
			ViewUtil.setHorizontalMargin(header.getRoot(), insets.left, insets.right);
		});

		refresher = new SwipeRefreshLayout(this);
		linear.addView(refresher, ViewUtil.createLinearParams(ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT));

		refresher.setOnRefreshListener(() -> {
			this.didReachedEnd = false;
			search(0);
		});

		var refresherContent = new LinearLayoutCompat(this);
		refresherContent.setOrientation(LinearLayoutCompat.VERTICAL);
		refresher.addView(refresherContent);

		loadingAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var binding = LayoutLoadingBinding.inflate(getLayoutInflater(), parent, false);
			ViewUtil.useLayoutParams(binding.getRoot(), params -> params.width = ViewUtil.MATCH_PARENT);
			return binding;
		}, LOADING_VIEW_TYPE);

		loadingAdapter.setEnabled(false);

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), adapter, loadingAdapter);

		var columnsCount = new AtomicInt(3);
		var layoutManager = new GridLayoutManager(this, columnsCount.get());

		recycler = new RecyclerView(this);
		recycler.setLayoutManager(layoutManager);
		recycler.setClipToPadding(false);
		recycler.setAdapter(concatAdapter);
		refresherContent.addView(recycler);

		recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				if(newState == RecyclerView.SCROLL_STATE_IDLE && !isLoading && !didReachedEnd
				&& layoutManager.findLastVisibleItemPosition() >= (items.size() - 1)) {
					search(currentPage + 1);
				}
			}
		});

		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				/* Don't ask. I don't know how it is working, so please don't ask about it. */
				return (concatAdapter.getItemViewType(position) == LOADING_VIEW_TYPE) ? 1 : columnsCount.get();
			}
		});

		ViewUtil.setOnApplyUiInsetsListener(recycler, insets -> {
			var padding = ViewUtil.dpPx(8);
			ViewUtil.setVerticalPadding(recycler, padding + padding * 2);
			ViewUtil.setHorizontalPadding(recycler, insets.left + padding, insets.right + padding);

			float columnSize = ViewUtil.dpPx(110);
			float freeSpace = getResources().getDisplayMetrics().widthPixels - (padding * 2) - insets.left - insets.right;
			columnsCount.set((int)(freeSpace / columnSize));
			layoutManager.setSpanCount(columnsCount.get());
		});
	}

	private void search(int page) {
		if(searchQuery == null || searchQuery.isBlank()) return;
		var wasSearchId = ++searchId;

		if(page == 0) {
			this.items.clear();
			adapter.setItems(this.items);
		}

		loadingAdapter.getBinding((binding, didJustCreated) -> {
			binding.progressBar.setVisibility(View.VISIBLE);
			binding.info.setVisibility(View.GONE);
		});

		loadingAdapter.setEnabled(true);

		AnilistSearchQuery.builder()
				.setSearchQuery(searchQuery)
				.setIsAdult(false)
				.setType(AnilistMedia.MediaType.ANIME)
				.setPage(page)
				.build()
				.executeQuery(items -> {
					if(wasSearchId != searchId) return;

					runOnUiThread(() -> {
						if(wasSearchId != searchId) return;
						this.isLoading = false;
						this.currentPage = page;

						if(page == 0) {
							this.items.addAll(items);
							adapter.setItems(this.items);
							return;
						}

						var wasSize = this.items.size();
						this.items.addAll(items, false);
						adapter.notifyItemRangeInserted(wasSize, items.size());
					});
				}).catchExceptions(e -> {
					if(wasSearchId != searchId) return;

					var error = new ExceptionDescriptor(e);
					Log.e(TAG, "Failed to search", e);

					runOnUiThread(() -> {
						if(wasSearchId != searchId) return;

						loadingAdapter.getBinding((binding, didJustCreated) -> {
							if(wasSearchId != searchId) return;

							if(page == 0) {
								this.items.clear();
								adapter.setItems(this.items);
							}

							if(e instanceof ZeroResultsException && page != 0) {
								this.didReachedEnd = true;
								binding.title.setText("You've reached the end.");
								binding.message.setText("No more results was found. If you didn't found what wanted, then try to change your query.");
							} else {
								binding.title.setText(error.getTitle(this));
								binding.message.setText(error.getMessage(this));
							}

							binding.progressBar.setVisibility(View.GONE);
							binding.info.setVisibility(View.VISIBLE);

							this.isLoading = false;
						});
					});
				}).onFinally(() -> {
					if(wasSearchId != searchId) return;
					refresher.setRefreshing(false);
				});
	}

	private static class Adapter extends RecyclerView.Adapter<ViewHolder> implements ObservableList.AddObserver<CatalogMedia>, ObservableList.RemoveObserver<CatalogMedia> {
		private ObservableList<CatalogMedia> items = ObservableEmptyList.getInstance();

		public Adapter() {
			setHasStableIds(true);
		}

		@SuppressLint("NotifyDataSetChanged")
		public void setItems(ObservableList<CatalogMedia> items) {
			if(this.items != null) {
				this.items.removeAdditionObserver(this);
				this.items.removeRemovalObserver(this);
			}

			if(items == null) {
				this.items = ObservableEmptyList.getInstance();
				notifyDataSetChanged();
				return;
			}

			this.items = items;
			items.observeAdditions(this);
			items.observeRemovals(this);
			notifyDataSetChanged();
		}

		@Override
		public long getItemId(int position) {
			return items.get(position).id;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = ItemGridMediaCatalogBinding.inflate(inflater, parent, false);
			var viewHolder = new ViewHolder(binding);

			var params = binding.getRoot().getLayoutParams();
			params.width = ViewUtil.MATCH_PARENT;

			if(!ViewUtil.setHorizontalMargin(params, ViewUtil.dpPx(6))) {
				throw new IllegalStateException("Failed to set horizontal margin for ItemGridMediaCatalogBinding!");
			}

			binding.getRoot().setLayoutParams(params);

			binding.getRoot().setOnClickListener(view -> MediaUtils.launchMediaActivity(parent.getContext(), viewHolder.getItem()));

			binding.getRoot().setOnLongClickListener(view -> {
				MediaUtils.openMediaActionsMenu(parent.getContext(), viewHolder.getItem());
				return true;
			});

			return viewHolder;
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@Override
		public void added(CatalogMedia item, int index) {
			notifyItemInserted(index);
		}

		@Override
		public void removed(CatalogMedia item, int index) {
			notifyItemRemoved(index);
		}
	}

	private static class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemGridMediaCatalogBinding binding;
		private CatalogMedia item;

		public ViewHolder(@NonNull ItemGridMediaCatalogBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia getItem() {
			return item;
		}

		public void bind(@NonNull CatalogMedia item) {
			this.item = item;

			binding.title.setText(item.title);
			binding.ongoing.setVisibility(item.status == CatalogMedia.MediaStatus.ONGOING ? View.VISIBLE : View.GONE);

			if(item.averageScore != null) {
				binding.scoreWrapper.setVisibility(View.VISIBLE);
				binding.score.setText(String.valueOf(item.averageScore));
			} else {
				binding.scoreWrapper.setVisibility(View.GONE);
			}

			try {
				Glide.with(binding.getRoot())
						.load(item.poster.large)
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(binding.mediaItemBanner);
			} catch(IllegalArgumentException e) {
				Log.e(TAG, "Failed to load a poster", e);
			}
		}
	}
}