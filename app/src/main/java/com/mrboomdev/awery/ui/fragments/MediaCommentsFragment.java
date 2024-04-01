package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.runOnUiThread;
import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.databinding.LayoutLoadingBinding;
import com.mrboomdev.awery.databinding.WidgetCommentBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.extensions.support.template.CatalogComment;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.util.List;

public class MediaCommentsFragment extends Fragment {
	private final SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> loadingAdapter;
	private final CommentsAdapter commentsAdapter = new CommentsAdapter();
	private SwipeRefreshLayout swipeRefresher;
	private ConcatAdapter adapter;
	private List<ExtensionProvider> providers;
	private CatalogMedia media;

	public MediaCommentsFragment() {
		this(null);
	}

	public MediaCommentsFragment(CatalogMedia media) {
		loadingAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = LayoutInflater.from(parent.getContext());
			return LayoutLoadingBinding.inflate(inflater, parent, false);
		});

		setMedia(media);
	}

	public void setMedia(CatalogMedia media) {
		if(media == null) return;
		this.media = media;
		if(adapter == null) return;

		loadData();
	}

	private void loadData() {
		if(providers != null) {
			if(providers.isEmpty()) {
				loadingAdapter.getBinding(binding -> {
					binding.title.setText(R.string.nothing_found);
					binding.message.setText(R.string.no_comment_extensions_message);

					binding.info.setVisibility(View.VISIBLE);
					binding.progressBar.setVisibility(View.GONE);
				});

				swipeRefresher.setRefreshing(false);
				return;
			}

			if(providers.size() > 1) {
				toast("Sorry, but you cannot currently use more than 1 comment extension :(");
			}

			var request = new ReadMediaCommentsRequest()
					.setMedia(media);

			providers.get(0).readMediaComments(request, new ExtensionProvider.ResponseCallback<>() {
				@Override
				public void onSuccess(CatalogComment parent) {
					runOnUiThread(() -> {
						if(getContext() == null) return;

						swipeRefresher.setRefreshing(false);
						loadingAdapter.setEnabled(false);
						commentsAdapter.setData(parent);
					});
				}

				@Override
				public void onFailure(Throwable e) {
					loadingAdapter.getBinding(binding -> runOnUiThread(() -> {
						if(getContext() == null) return;
						swipeRefresher.setRefreshing(false);

						var descriptor = new ExceptionDescriptor(e);
						binding.title.setText(descriptor.getTitle(getContext()));
						binding.message.setText(descriptor.getMessage(getContext()));

						binding.info.setVisibility(View.VISIBLE);
						binding.progressBar.setVisibility(View.GONE);

						loadingAdapter.setEnabled(true);
					}));

					Log.e("MediaCommentsFragment", "Failed to load comments!", e);
				}
			});
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		providers = stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
				.map(extension -> extension.getProviders(ExtensionProvider.FEATURE_MEDIA_COMMENTS))
				.flatMap(AweryApp::stream)
				.sorted().toList();

		setMedia(media);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		swipeRefresher = new SwipeRefreshLayout(inflater.getContext());
		swipeRefresher.setOnRefreshListener(this::loadData);

		var recycler = new RecyclerView(inflater.getContext());
		recycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
		recycler.setClipToPadding(false);

		ViewUtil.setOnApplyUiInsetsListener(recycler, insets -> {
			var padding = dpPx(8);

			setTopPadding(recycler, insets.top + padding);
			setRightPadding(recycler, insets.right + padding);
			setLeftPadding(recycler, padding);
			setBottomPadding(recycler, padding);
		}, container);

		adapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS).build(),
				loadingAdapter, commentsAdapter);

		recycler.setAdapter(adapter);

		swipeRefresher.addView(recycler);
		return swipeRefresher;
	}

	private static class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
		private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
		private CatalogComment root;

		public CommentsAdapter() {
			setHasStableIds(true);
		}

		@SuppressLint("NotifyDataSetChanged")
		public void setData(@NonNull CatalogComment root) {
			this.root = root;
			idGenerator.clear();

			for(var item : root.items) {
				item.visualId = idGenerator.getLong();
			}

			notifyDataSetChanged();
		}

		@NonNull
		@Override
		public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			return new CommentViewHolder(WidgetCommentBinding.inflate(inflater, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
			var item = root.items.get(position);
			holder.bind(item);
		}

		@Override
		public long getItemId(int position) {
			var item = root.items.get(position);
			return item.visualId;
		}

		@Override
		public int getItemCount() {
			if(root == null) return 0;
			return root.items.size();
		}
	}

	private static class CommentViewHolder extends RecyclerView.ViewHolder {
		private final WidgetCommentBinding binding;

		public CommentViewHolder(@NonNull WidgetCommentBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(@NonNull CatalogComment comment) {
			binding.name.setText(comment.authorName);
			binding.message.setText(comment.text);

			binding.likesCount.setText(String.valueOf(comment.likes));
			binding.dislikesCount.setText(String.valueOf(comment.dislikes));
			binding.votesCount.setText(String.valueOf(comment.votes));

			if(comment.likes == CatalogComment.DISABLED) {
				binding.likeIcon.setVisibility(View.GONE);
				binding.likesCount.setVisibility(View.GONE);
				binding.likeButton.setVisibility(View.GONE);
			} else if(comment.likes == CatalogComment.HIDDEN) {
				binding.likeIcon.setVisibility(View.VISIBLE);
				binding.likesCount.setVisibility(View.GONE);
				binding.likeButton.setVisibility(View.VISIBLE);
			} else {
				binding.likeIcon.setVisibility(View.VISIBLE);
				binding.likesCount.setVisibility(View.VISIBLE);
				binding.likeButton.setVisibility(View.VISIBLE);
			}

			if(comment.dislikes == CatalogComment.DISABLED) {
				binding.dislikeIcon.setVisibility(View.GONE);
				binding.dislikesCount.setVisibility(View.GONE);
				binding.dislikeButton.setVisibility(View.GONE);
			} else if(comment.dislikes == CatalogComment.HIDDEN) {
				binding.dislikeIcon.setVisibility(View.VISIBLE);
				binding.dislikesCount.setVisibility(View.GONE);
				binding.dislikeButton.setVisibility(View.VISIBLE);
			} else {
				binding.dislikeIcon.setVisibility(View.VISIBLE);
				binding.dislikesCount.setVisibility(View.VISIBLE);
				binding.dislikeButton.setVisibility(View.VISIBLE);
			}

			if(comment.votes >= 0) binding.votesCount.setVisibility(View.VISIBLE);
			else binding.votesCount.setVisibility(View.GONE);

			Glide.with(binding.icon)
					.clear(binding.icon);

			Glide.with(binding.icon)
					.load(comment.authorAvatar)
					.transition(DrawableTransitionOptions.withCrossFade())
					.into(binding.icon);
		}
	}
}