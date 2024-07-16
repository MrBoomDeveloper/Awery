package com.mrboomdev.awery.ui.activity.search;

import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryLifecycle;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.databinding.GridMediaCatalogBinding;
import com.mrboomdev.awery.databinding.ScreenSearchBinding;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.sheet.FiltersSheet;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity extends AppCompatActivity {
	public static final String ACTION_PICK_MEDIA = "pick_media";
	public static final String ACTION_SEARCH_BY_TAG = "searchByTag";
	public static final String EXTRA_TAG = "tag";
	public static final String EXTRA_FILTERS = "filters";
	public static final String EXTRA_LOADED_MEDIA = "loadedMedia";
	/**
	 * It is meant that the provider can be found by using {@code ExtensionProvider.forGlobalId(String)}
	 */
	public static final String EXTRA_GLOBAL_PROVIDER_ID = "source";
	public static final String RESULT_EXTRA_MEDIA = "media";
	private static final int LOADING_VIEW_TYPE = 1;
	private static final String SAVED_FILTERS = "filters";
	private static final String SAVED_ITEMS = "items";
	private static final String SAVED_DID_REACHED_END = "did_reached_end";
	private static final String TAG = "SearchActivity";
	private final WeakHashMap<CatalogMedia, Long> ids = new WeakHashMap<>();
	private final Adapter adapter = new Adapter();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private SettingsList filters = new SettingsList();
	private ArrayList<CatalogMedia> items = new ArrayList<>();
	private ScreenSearchBinding binding;
	private SingleViewAdapter.BindingSingleViewAdapter<EmptyView> loadingAdapter;
	private ExtensionProvider source;
	private boolean didReachedEnd;
	private int searchId;

	private SettingsItem queryFilter = new SettingsItem(
			SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY);

	private SettingsItem pageFilter = new SettingsItem(
			SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0);

	/** We initially set this value to "true" so that list won't try
	 to load anything because we haven't typed anything yet. **/
	private boolean isLoading = true;

	@SuppressLint("NotifyDataSetChanged")
	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			filters = (SettingsList) savedInstanceState.getSerializable(SAVED_FILTERS);
			items = (ArrayList<CatalogMedia>) savedInstanceState.getSerializable(SAVED_ITEMS);
			didReachedEnd = savedInstanceState.getBoolean(SAVED_DID_REACHED_END);
		}

		var columnsCountLand = new AtomicInteger(AwerySettings.MEDIA_COLUMNS_COUNT_LAND.getValue());
		var columnsCountPort = new AtomicInteger(AwerySettings.MEDIA_COLUMNS_COUNT_PORT.getValue());

		var autoColumnsCountLand = columnsCountLand.get() == 0;
		var autoColumnsCountPort = columnsCountPort.get() == 0;

		var filters = (List<SettingsItem>) getIntent().getSerializableExtra(EXTRA_FILTERS);

		if(filters != null) {
			var foundQuery = find(filters, filter -> Objects.equals(filter.getKey(), ExtensionProvider.FILTER_QUERY));
			if(foundQuery != null) queryFilter.setValue(foundQuery.getStringValue());
		}

		if(savedInstanceState == null) applyFilters(filters, true);
		else applyFilters(this.filters, false);

		try {
			this.source = ExtensionProvider.forGlobalId(Objects.requireNonNull(
					getIntent().getStringExtra(EXTRA_GLOBAL_PROVIDER_ID)));
		} catch(ExtensionNotInstalledException e) {
			toast("Source extension isn't installed!");
			finish();
		}

		binding = ScreenSearchBinding.inflate(getLayoutInflater());
		binding.header.edittext.setText(queryFilter.getStringValue());
		binding.header.edittext.requestFocus();
		binding.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));

		binding.header.back.setOnClickListener(v -> finish());
		binding.header.clear.setOnClickListener(v -> binding.header.edittext.setText(null));

		binding.header.filters.setOnClickListener(v -> new FiltersSheet(this, this.filters, source, newFilters -> {
			applyFilters(newFilters, true);
			didReachedEnd = false;
			search(0);
		}).show());

		var inputManager = getSystemService(InputMethodManager.class);

		binding.header.edittext.setOnEditorActionListener((v, action, event) -> {
			if(action != EditorInfo.IME_ACTION_SEARCH) {
				return false;
			}

			inputManager.hideSoftInputFromWindow(
					binding.header.edittext.getWindowToken(), 0);

			queryFilter.setValue(v.getText().toString());
			didReachedEnd = false;

			search(0);
			return true;
		});

		setOnApplyUiInsetsListener(binding.header.getRoot(), insets -> {
			setTopMargin(binding.header.getRoot(), insets.top);
			setHorizontalMargin(binding.header.getRoot(), insets.left, insets.right);
			return true;
		});

		binding.swipeRefresher.setOnRefreshListener(() -> {
			this.didReachedEnd = false;
			search(0);
		});

		binding.swipeRefresher.setColorSchemeColors(resolveAttrColor(
				this, android.R.attr.colorPrimary));

		binding.swipeRefresher.setProgressBackgroundColorSchemeColor(resolveAttrColor(
				this, com.google.android.material.R.attr.colorSurface));

		loadingAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var binding = new EmptyView(parent, false);
			ViewUtil.useLayoutParams(binding.getRoot(), params -> params.width = ViewUtil.MATCH_PARENT);
			return binding;
		}, LOADING_VIEW_TYPE);

		loadingAdapter.setEnabled(false);

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), adapter, loadingAdapter);

		var layoutManager = new GridLayoutManager(this, isLandscape()
				? (autoColumnsCountLand ? 3 : columnsCountLand.get())
				: (autoColumnsCountPort ? 5 : columnsCountPort.get()));

		binding.recycler.setLayoutManager(layoutManager);
		binding.recycler.setAdapter(concatAdapter);

		binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				tryLoadMore();
			}
		});

		setOnApplyUiInsetsListener(binding.recycler, insets -> {
			var padding = dpPx(binding.recycler, 8);
			setVerticalPadding(binding.recycler, padding + padding * 2);
			setHorizontalPadding(binding.recycler, insets.left + padding, insets.right + padding);

			if(isLandscape() && autoColumnsCountLand) {
				float columnSize = dpPx(binding, 110);
				float freeSpace = getResources().getDisplayMetrics().widthPixels - (padding * 2) - insets.left - insets.right;
				columnsCountLand.set((int)(freeSpace / columnSize));
				layoutManager.setSpanCount(columnsCountLand.get());
			} else if(!isLandscape() && autoColumnsCountPort) {
				float columnSize = dpPx(binding, 110);
				float freeSpace = getResources().getDisplayMetrics().widthPixels - (padding * 2) - insets.left - insets.right;
				columnsCountPort.set((int)(freeSpace / columnSize));
				layoutManager.setSpanCount(columnsCountPort.get());
			}

			return true;
		});

		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return (position < items.size()) ? 1 : (isLandscape()
						? (columnsCountLand.get() == 0 ? layoutManager.getSpanCount() : columnsCountLand.get())
						: columnsCountPort.get() == 0 ? layoutManager.getSpanCount() : columnsCountPort.get());
			}
		});

		if(items.isEmpty()) {
			doIfNotNull((List<CatalogMedia>) getIntent().getSerializableExtra(EXTRA_LOADED_MEDIA), loadedMedia -> {
				for(var item : loadedMedia) {
					ids.put(item, idGenerator.getLong());
				}

				isLoading = false;
				items.addAll(loadedMedia);
				adapter.notifyItemRangeInserted(0, items.size());
			});
		} else {
			for(var item : items) {
				ids.put(item, idGenerator.getLong());
			}

			isLoading = false;
		}

		setContentView(binding.getRoot());

		if(ACTION_SEARCH_BY_TAG.equals(getIntent().getAction())) {
			var tag = requireNonNull(getIntent().getStringExtra(EXTRA_TAG)).trim();
			didReachedEnd = true;

			binding.headerWrapper.setVisibility(View.GONE);
			binding.swipeRefresher.setEnabled(false);


			loadingAdapter.setEnabled(true);
			loadingAdapter.getBinding(EmptyView::startLoading);

			source.getFilters(new ExtensionProvider.ResponseCallback<>() {

				private void done() {
					SearchActivity.this.binding.headerWrapper.setVisibility(View.VISIBLE);
					SearchActivity.this.binding.swipeRefresher.setEnabled(true);

					didReachedEnd = false;
					search(0);
				}

				@Nullable
				private SettingsItem findTag(@NonNull List<? extends SettingsItem> items) {
					for(var item : items) {
						if(item.getItems() != null) {
							var found = findTag(item.getItems());

							if(found != null) {
								return found;
							}
						}

						var title = item.getTitle(SearchActivity.this);
						if(title == null) continue;

						if(requireNonNull(tag).equalsIgnoreCase(title.trim())) {
							return item;
						}
					}

					return null;
				}

				private void activate(@NonNull SettingsItem item, SettingsItem parent) {
					if(item.getType() != null) {
						switch(item.getType()) {
							case BOOLEAN, SCREEN_BOOLEAN -> item.setValue(true);
							case EXCLUDABLE -> item.setValue(Selection.State.SELECTED);
						}
					} else if(parent != null) {
						switch(parent.getType()) {
							case SELECT, SELECT_INTEGER -> parent.setValue(item.getKey());
							case MULTISELECT -> parent.setValue(Set.of(item.getKey()));
						}
					} else {
						onFailure(null);
					}
				}

				@Override
				public void onSuccess(List<SettingsItem> items) {
					for(var item : items) {
						item.setAsParentForChildren();
					}

					var found = findTag(items);

					if(found == null) {
						onFailure(null);
					} else {
						SettingsItem parent = found, root = parent;

						while((parent = parent.getParent()) != null) {
							root = parent;
						}

						activate(found, found.getParent());
						applyFilters(List.of(root), true);
						AweryLifecycle.runOnUiThread(this::done, SearchActivity.this.binding.recycler);
					}
				}

				@Override
				public void onFailure(@Nullable Throwable e) {
					if(e != null) {
						Log.e(TAG, "Failed to fetch filters!", e);
					}

					queryFilter.setValue(tag);

					AweryLifecycle.runOnUiThread(() -> {
						SearchActivity.this.binding.header.edittext.setText(tag);
						done();
					}, SearchActivity.this.binding.recycler);
				}
			});
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(SAVED_FILTERS, filters);
		outState.putSerializable(SAVED_ITEMS, items);
		outState.putBoolean(SAVED_DID_REACHED_END, didReachedEnd);
		super.onSaveInstanceState(outState);
	}

	private void applyFilters(@Nullable List<SettingsItem> newFilters, boolean ignoreInternalFilters) {
		this.filters.clear();

		if(newFilters != null) {
			this.filters.addAll(newFilters);

			var foundQuery = find(this.filters, filter -> Objects.equals(filter.getKey(), ExtensionProvider.FILTER_QUERY));
			var foundPage = find(this.filters, filter -> Objects.equals(filter.getKey(), ExtensionProvider.FILTER_PAGE));

			if(foundQuery != null) {
				if(ignoreInternalFilters) this.filters.remove(foundQuery);
				else queryFilter = foundQuery;
			}

			if(foundPage != null) {
				if(ignoreInternalFilters) this.filters.remove(foundPage);
				else pageFilter = foundPage;
			}
		}

		if(ignoreInternalFilters) {
			this.filters.add(queryFilter);
			this.filters.add(pageFilter);
		}
	}

	private void tryLoadMore() {
		if(!isLoading && !didReachedEnd) {
			var lastIndex = items.size() - 1;

			if(binding.recycler.getLayoutManager() instanceof LinearLayoutManager manager
					&& manager.findLastVisibleItemPosition() >= lastIndex) {
				pageFilter.setValue(pageFilter.getIntegerValue() + 1);
				search(pageFilter.getIntegerValue());
			}
		}
	}

	private void reachedEnd(long wasSearchId) {
		loadingAdapter.getBinding((binding) -> runOnUiThread(() -> {
			if(wasSearchId != searchId) return;

			SearchActivity.this.didReachedEnd = true;
			binding.title.setText(R.string.you_reached_end);
			binding.message.setText(R.string.you_reached_end_description);

			binding.progressBar.setVisibility(View.GONE);
			binding.info.setVisibility(View.VISIBLE);

			isLoading = false;
			didReachedEnd = true;
		}));
	}

	@SuppressLint("NotifyDataSetChanged")
	private void search(int page) {
		if(queryFilter.getStringValue() == null) {
			queryFilter.setValue("");
		}

		var wasSearchId = ++searchId;

		if(page == 0) {
			this.items.clear();
			adapter.notifyDataSetChanged();
			idGenerator.clear();
		}

		loadingAdapter.getBinding((binding) -> {
			binding.progressBar.setVisibility(View.VISIBLE);
			binding.info.setVisibility(View.GONE);
		});

		loadingAdapter.setEnabled(true);
		pageFilter.setValue(page);

		source.searchMedia(this, filters, new ExtensionProvider.ResponseCallback<>() {
			@SuppressLint("NotifyDataSetChanged")
			@Override
			public void onSuccess(CatalogSearchResults<? extends CatalogMedia> items) {
				if(wasSearchId != searchId) return;

				if(!items.hasNextPage()) {
					reachedEnd(wasSearchId);
				}

				MediaUtils.filterMedia(items, filteredItems -> {
					if(filteredItems.isEmpty()) {
						throw new ZeroResultsException("No media was found", R.string.no_media_found);
					}

					for(var item : filteredItems) {
						ids.put(item, idGenerator.getLong());
					}

					runOnUiThread(() -> {
						if(wasSearchId != searchId) return;
						SearchActivity.this.isLoading = false;

						if(page == 0) {
							SearchActivity.this.items.addAll(filteredItems);
							adapter.notifyDataSetChanged();
						} else {
							var wasSize = SearchActivity.this.items.size();
							SearchActivity.this.items.addAll(filteredItems);
							adapter.notifyItemRangeInserted(wasSize, filteredItems.size());
						}

						runDelayed(() -> tryLoadMore(), 1000, binding.recycler);
					});
				});

				onFinally();
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			public void onFailure(Throwable e) {
				if(wasSearchId != searchId) return;

				var error = new ExceptionDescriptor(e);
				Log.e(TAG, "Failed to search", e);

				runOnUiThread(() -> {
					if(wasSearchId != searchId) return;

					loadingAdapter.getBinding((binding) -> {
						if(wasSearchId != searchId) return;

						if(page == 0) {
							SearchActivity.this.items.clear();
							adapter.notifyDataSetChanged();
						}

						if(e instanceof ZeroResultsException && page != 0) {
							reachedEnd(wasSearchId);
						} else {
							binding.title.setText(error.getTitle(SearchActivity.this));
							binding.message.setText(error.getMessage(SearchActivity.this));
						}

						binding.progressBar.setVisibility(View.GONE);
						binding.info.setVisibility(View.VISIBLE);

						SearchActivity.this.isLoading = false;
					});
				});

				onFinally();
			}

			private void onFinally() {
				if(wasSearchId != searchId) return;
				binding.swipeRefresher.setRefreshing(false);
			}
		});
	}

	private class Adapter extends RecyclerView.Adapter<ViewHolder> {

		public Adapter() {
			setHasStableIds(true);
		}

		@Override
		public long getItemId(int position) {
			return Objects.requireNonNull(ids.get(items.get(position)));
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = GridMediaCatalogBinding.inflate(inflater, parent, false);
			var viewHolder = new ViewHolder(binding);

			useLayoutParams(binding.getRoot(), params -> {
				params.width = ViewUtil.MATCH_PARENT;
				setHorizontalMargin(params, dpPx(binding, 6));
			}, RecyclerView.LayoutParams.class);

			binding.getRoot().setOnClickListener(view -> {
				if(ACTION_PICK_MEDIA.equals(getIntent().getAction())) {
					setResult(0, new Intent().putExtra(RESULT_EXTRA_MEDIA, viewHolder.getItem()));
					finish();
					return;
				}

				MediaUtils.launchMediaActivity(parent.getContext(), viewHolder.getItem());
			});

			binding.getRoot().setOnLongClickListener(view -> {
				var media = viewHolder.getItem();
				var index = items.indexOf(media);

				MediaUtils.openMediaActionsMenu(parent.getContext(), media,
						() -> MediaUtils.isMediaFiltered(media, isFiltered -> {
					if(!isFiltered) return;

					AweryLifecycle.runOnUiThread(() -> {
						items.remove(media);
						notifyItemRemoved(index);
					});
				}));
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
	}

	private static class ViewHolder extends RecyclerView.ViewHolder {
		private final GridMediaCatalogBinding binding;
		private CatalogMedia item;

		public ViewHolder(@NonNull GridMediaCatalogBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia getItem() {
			return item;
		}

		public void bind(@NonNull CatalogMedia item) {
			this.item = item;

			binding.title.setText(item.getTitle());
			binding.ongoing.setVisibility(item.status == CatalogMedia.MediaStatus.ONGOING ? View.VISIBLE : View.GONE);

			if(item.averageScore != null) {
				binding.scoreWrapper.setVisibility(View.VISIBLE);
				binding.score.setText(String.valueOf(item.averageScore));
			} else {
				binding.scoreWrapper.setVisibility(View.GONE);
			}

			try {
				Glide.with(binding.getRoot())
						.load(item.getLargePoster())
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(binding.mediaItemBanner);
			} catch(IllegalArgumentException e) {
				Log.e(TAG, "Failed to load a poster", e);
			}
		}
	}
}