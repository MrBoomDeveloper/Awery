package com.mrboomdev.awery.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
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
import com.mrboomdev.awery.util.exceptions.ExceptionUtil;
import com.mrboomdev.awery.util.observable.ObservableArrayList;
import com.mrboomdev.awery.util.observable.ObservableEmptyList;
import com.mrboomdev.awery.util.observable.ObservableList;
import com.mrboomdev.awery.util.ui.ViewUtil;

public class SearchActivity extends AppCompatActivity {
	private static final String TAG = "SearchActivity";
	private final Adapter adapter = new Adapter();
	private final ObservableList<CatalogMedia> items = new ObservableArrayList<>();
	private LayoutHeaderSearchBinding header;
	private LayoutLoadingBinding loading;
	private SwipeRefreshLayout refresher;
	private RecyclerView recycler;
	private int searchId;

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

		header.edittext.setOnEditorActionListener((v, action, event) -> {
			if(action == EditorInfo.IME_ACTION_SEARCH) {
				search();
				return true;
			}

			return false;
		});

		ViewUtil.setOnApplyUiInsetsListener(header.getRoot(), insets -> {
			ViewUtil.setTopMargin(header.getRoot(), insets.top);
			ViewUtil.setHorizontalMargin(header.getRoot(), insets.left, insets.right);
		});

		refresher = new SwipeRefreshLayout(this);
		refresher.setOnRefreshListener(this::search);
		linear.addView(refresher, ViewUtil.createLinearParams(ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT));

		var refresherContent = new LinearLayoutCompat(this);
		refresherContent.setOrientation(LinearLayoutCompat.VERTICAL);
		refresher.addView(refresherContent);

		loading = LayoutLoadingBinding.inflate(getLayoutInflater(), refresherContent, true);
		loading.getRoot().setVisibility(View.GONE);

		var layoutManager = new GridLayoutManager(this, 3);
		recycler = new RecyclerView(this);
		recycler.setLayoutManager(layoutManager);
		recycler.setClipToPadding(false);
		recycler.setAdapter(adapter);
		refresherContent.addView(recycler);

		ViewUtil.setOnApplyUiInsetsListener(recycler, insets -> {
			var padding = ViewUtil.dpPx(8);
			ViewUtil.setVerticalPadding(recycler, padding + padding * 2);
			ViewUtil.setHorizontalPadding(recycler, insets.left + padding, insets.right + padding);

			float columnSize = ViewUtil.dpPx(110);
			float freeSpace = getResources().getDisplayMetrics().widthPixels - (padding * 2) - insets.left - insets.right;
			var columnsCount = (int)(freeSpace / columnSize);
			layoutManager.setSpanCount(columnsCount);
		});
	}

	private void search() {
		var text = header.edittext.getText().toString();
		if(text.isBlank()) return;
		var wasSearchId = ++searchId;

		loading.progressBar.setVisibility(View.VISIBLE);
		loading.info.setVisibility(View.GONE);

		loading.getRoot().setVisibility(View.VISIBLE);
		recycler.setVisibility(View.GONE);

		AnilistSearchQuery.builder()
				.setSearchQuery(text)
				.setIsAdult(false)
				.setType(AnilistMedia.MediaType.ANIME)
				.build()
				.executeQuery(items -> {
					if(wasSearchId != searchId) return;

					runOnUiThread(() -> {
						if(wasSearchId != searchId) return;

						this.items.clear();
						this.items.addAll(items);

						adapter.setItems(this.items);
						loading.getRoot().setVisibility(View.GONE);
						recycler.setVisibility(View.VISIBLE);
					});
				}).catchExceptions(e -> {
					if(wasSearchId != searchId) return;

					var error = new ExceptionUtil(e);
					Log.e(TAG, "Failed to search", e);

					runOnUiThread(() -> {
						if(wasSearchId != searchId) return;

						loading.title.setText(error.getTitle(this));
						loading.message.setText(error.getMessage(this));

						loading.progressBar.setVisibility(View.GONE);
						loading.info.setVisibility(View.VISIBLE);

						loading.getRoot().setVisibility(View.VISIBLE);
						recycler.setVisibility(View.GONE);
					});
				}).onFinally(() -> {
					if(wasSearchId != searchId) return;
					refresher.setRefreshing(false);
				});
	}

	private static class Adapter extends RecyclerView.Adapter<ViewHolder> implements ObservableList.AddObserver<CatalogMedia>, ObservableList.RemoveObserver<CatalogMedia> {
		private ObservableList<CatalogMedia> items = ObservableEmptyList.getInstance();

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