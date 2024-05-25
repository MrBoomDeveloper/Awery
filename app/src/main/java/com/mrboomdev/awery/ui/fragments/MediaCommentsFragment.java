package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.addOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.createLinearParams;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.databinding.ItemListDropdownBinding;
import com.mrboomdev.awery.databinding.LayoutCommentsHeaderBinding;
import com.mrboomdev.awery.databinding.LayoutLoadingBinding;
import com.mrboomdev.awery.databinding.WidgetCommentBinding;
import com.mrboomdev.awery.databinding.WidgetCommentSendBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.request.PostMediaCommentRequest;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.sdk.util.exceptions.InvalidSyntaxException;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.ArrayListAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import java9.util.Objects;

public class MediaCommentsFragment extends Fragment {
	private static final int LAST_PAGE = -1;
	private static final String TAG = "MediaCommentsFragment";
	private final SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> loadingAdapter;
	private final SingleViewAdapter.BindingSingleViewAdapter<LayoutCommentsHeaderBinding> headerAdapter;
	private final ArrayListAdapter<ExtensionProvider> sourcesAdapter;
	private final CommentsAdapter commentsAdapter = new CommentsAdapter();
	private final ConcatAdapter concatAdapter;
	private final WeakHashMap<CatalogComment, Parcelable> scrollPositions = new WeakHashMap<>();
	private final WeakHashMap<CatalogComment, Integer> pages = new WeakHashMap<>();
	private final List<CatalogComment> currentCommentsPath = new ArrayList<>();
	private final Runnable backPressCallback;
	private List<ExtensionProvider> sources;
	private boolean isLoading;
	private ExtensionProvider selectedProvider;
	private RecyclerView recycler;
	private Runnable onCloseRequestListener;
	private WidgetCommentSendBinding sendBinding;
	private SwipeRefreshLayout swipeRefresher;
	private CatalogMedia media;
	private CatalogComment comment, editedComment;

	public MediaCommentsFragment() {
		this(null);
	}

	public MediaCommentsFragment(CatalogMedia media) {
		loadingAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = LayoutInflater.from(parent.getContext());
			return LayoutLoadingBinding.inflate(inflater, parent, false);
		});

		sourcesAdapter = new ArrayListAdapter<>((item, recycled, parentView) -> {
			if(recycled == null) {
				var inflater = LayoutInflater.from(parentView.getContext());

				recycled = ItemListDropdownBinding.inflate(
						inflater, parentView, false).getRoot();
			}

			TextView textView = recycled.findViewById(R.id.title);
			textView.setText(item.getName());

			ImageView icon = recycled.findViewById(R.id.icon);
			icon.setVisibility(View.GONE);

			return recycled;
		});

		headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = LayoutCommentsHeaderBinding.inflate(inflater, parent, false);

			binding.searchWrapper.setVisibility(View.GONE);
			binding.seasonWrapper.setVisibility(View.GONE);

			binding.sourceDropdown.setAdapter(sourcesAdapter);

			binding.sourceDropdown.setOnItemClickListener((dropdownParent, _view, position, id) -> {
				selectedProvider = sources.get(position);
				setSource(selectedProvider);
			});

			binding.episodeDropdown.setOnEditorActionListener((v, actionId, event) -> {
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					var text = v.getText().toString();
					setSource(selectedProvider);
					return true;
				}

				return false;
			});

			return binding;
		});

		concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS).build(),
				headerAdapter, commentsAdapter, loadingAdapter);

		backPressCallback = () -> {
			if(!currentCommentsPath.isEmpty() && !isLoading) {
				currentCommentsPath.remove(currentCommentsPath.size() - 1);
			}

			if(currentCommentsPath.isEmpty()) {
				if(onCloseRequestListener != null) {
					onCloseRequestListener.run();
					return;
				}

				requireActivity().finish();
				return;
			}

			setComment(currentCommentsPath.get(currentCommentsPath.size() - 1), null);
		};

		setMedia(media);
	}

	public void setEpisode(CatalogEpisode episode) {
		headerAdapter.getBinding(binding -> {
			if(episode != null) {
				var rounded = Math.round(episode.getNumber());

				binding.episodeDropdown.setText((rounded == episode.getNumber())
						? String.valueOf(rounded) : String.valueOf(episode.getNumber()));

				setSource(selectedProvider);
			} else {
				binding.episodeDropdown.setText(null, false);
			}
		});
	}

	private void setSource(ExtensionProvider source) {
		this.selectedProvider = source;

		loadingAdapter.getBinding(loadingBinding -> {
			loadingBinding.progressBar.setVisibility(View.VISIBLE);
			loadingBinding.info.setVisibility(View.GONE);
			loadingAdapter.setEnabled(true);
		});

		headerAdapter.getBinding(headerBinding -> {
			var isVisible = source != null && source.hasFeature(ExtensionProvider.FEATURE_COMMENTS_PER_EPISODE);
			headerBinding.episodeWrapper.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		});

		runOnUiThread(() -> commentsAdapter.setData(null), recycler);

		currentCommentsPath.clear();
		pages.clear();
		scrollPositions.clear();

		loadData(null, null, 0);
	}

	public void setOnCloseRequestListener(Runnable listener) {
		this.onCloseRequestListener = listener;
	}

	@Override
	public void onResume() {
		super.onResume();
		addOnBackPressedListener(requireActivity(), backPressCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		removeOnBackPressedListener(requireActivity(), backPressCallback);
	}

	public void setMedia(CatalogMedia media) {
		if(media == null) return;
		this.media = media;

		if(recycler == null) return;
		setSource(selectedProvider);
	}

	private void setComment(@Nullable CatalogComment comment, CatalogComment reloadThis) {
		this.recycler.scrollToPosition(0);
		this.comment = comment;

		sendBinding.editing.setVisibility(View.GONE);
		sendBinding.input.setText(null);
		editedComment = null;

		if(comment != null) {
			swipeRefresher.setRefreshing(false);
			loadingAdapter.setEnabled(false);

			headerAdapter.getBinding(binding ->
					binding.searchStatus.setText("Found " + comment.size() + " comments"));

			if(reloadThis == null) {
				if(!currentCommentsPath.contains(comment)) {
					currentCommentsPath.add(comment);
				}
			} else {
				var wasIndex = currentCommentsPath.indexOf(reloadThis);
				currentCommentsPath.remove(wasIndex);
				currentCommentsPath.add(wasIndex, comment);
			}
		}

		runOnUiThread(() -> commentsAdapter.setData(comment), recycler);

		//TODO: Load an avatar received from the extension
		sendBinding.avatarWrapper.setVisibility(View.GONE);

		sendBinding.getRoot().setVisibility(
				comment != null && comment.canComment ?
				View.VISIBLE : View.GONE);

		var scrollPosition = scrollPositions.get(comment);

		if(scrollPosition != null) {
			var layoutManager = requireNonNull(recycler.getLayoutManager());
			layoutManager.onRestoreInstanceState(scrollPosition);
		}

		if(comment != null && !comment.hasNextPage()) {
			reachedEnd();
		}
	}

	private void loadData(CatalogComment parent, CatalogComment reloadThis, int page) {
		if(this.comment != null) {
			var layoutManager = requireNonNull(recycler.getLayoutManager());
			scrollPositions.put(this.comment, layoutManager.onSaveInstanceState());
		}

		if(sources == null || sources.isEmpty()) {
			loadingAdapter.getBinding(binding -> {
				binding.title.setText(R.string.nothing_found);
				binding.message.setText(R.string.no_comment_extensions);

				binding.info.setVisibility(View.VISIBLE);
				binding.progressBar.setVisibility(View.GONE);
			});

			headerAdapter.getBinding(binding ->
					binding.searchStatus.setText("Nothing found"));

			swipeRefresher.setRefreshing(false);
			return;
		}

		if(reloadThis == null) {
			loadingAdapter.getBinding(binding -> {
				binding.info.setVisibility(View.GONE);
				binding.progressBar.setVisibility(View.VISIBLE);
			});

			loadingAdapter.setEnabled(true);
			setComment(null, null);
		}

		var request = new ReadMediaCommentsRequest()
				.setPage(page)
				.setParentComment(parent)
				.setMedia(media);

		getEpisodeNumber: try {
			var binding = headerAdapter.getBinding();

			if(binding != null) {
				var text = binding.episodeDropdown.getText().toString();
				if(text.isBlank()) break getEpisodeNumber;

				var number = Float.parseFloat(text);
				request.setEpisode(new CatalogEpisode(number));
			}
		} catch(NumberFormatException e) {
			headerAdapter.getBinding().episodeWrapper.setError(e.getMessage());
		}

		isLoading = true;

		headerAdapter.getBinding(binding ->
				binding.searchStatus.setText("Loading comments..."));

		selectedProvider.readMediaComments(request, new ExtensionProvider.ResponseCallback<>() {
			@Override
			public void onSuccess(CatalogComment newComment) {
				runOnUiThread(() -> {
					if(getContext() == null) return;
					isLoading = false;

					if(page == 0) {
						pages.put(newComment, 0);
						setComment(newComment, reloadThis);
						return;
					}

					commentsAdapter.addData(newComment);

					headerAdapter.getBinding(binding ->
							binding.searchStatus.setText("Found " + commentsAdapter.getItemCount() + " comments"));

					if(!newComment.hasNextPage()) {
						reachedEnd();
					}
				}, recycler);
			}

			@Override
			public void onFailure(Throwable e) {
				loadingAdapter.getBinding(binding -> runOnUiThread(() -> {
					if(getContext() == null) return;
					swipeRefresher.setRefreshing(false);
					isLoading = false;

					if(parent != null && (reloadThis == null ||
							(e instanceof JsException jsE && Objects.equals(jsE.getErrorId(), JsException.ERROR_NOTHING_FOUND)))) {
						setComment(parent, reloadThis);
						loadingAdapter.setEnabled(false);
						return;
					}

					var descriptor = new ExceptionDescriptor(e);
					binding.title.setText(descriptor.getTitle(getContext()));
					binding.message.setText(descriptor.getMessage(getContext()));

					binding.info.setVisibility(View.VISIBLE);
					binding.progressBar.setVisibility(View.GONE);

					commentsAdapter.setData(null);
					loadingAdapter.setEnabled(true);
					sendBinding.getRoot().setVisibility(View.GONE);

					headerAdapter.getBinding(headerBinding ->
							headerBinding.searchStatus.setText("Failed to load"));
				}, recycler));

				Log.e("MediaCommentsFragment", "Failed to load comments!", e);
			}
		});
	}

	private void reachedEnd() {
		pages.put(comment, LAST_PAGE);

		loadingAdapter.getBinding(binding -> {
			binding.info.setVisibility(View.VISIBLE);
			binding.progressBar.setVisibility(View.GONE);
			binding.title.setText(R.string.you_reached_end);
			binding.message.setText(R.string.you_reached_end_description);
			loadingAdapter.setEnabled(true);
		});
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		sources = stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
				.map(extension -> extension.getProviders(ExtensionProvider.FEATURE_MEDIA_COMMENTS))
				.flatMap(NiceUtils::stream)
				.sorted().toList();

		sourcesAdapter.setItems(sources);

		if(!sources.isEmpty()) {
			selectedProvider = sources.get(0);
			sourcesAdapter.setItems(sources);

			headerAdapter.getBinding(binding -> binding.sourceDropdown.setText(
					selectedProvider.getName(), false));
		}

		recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				tryLoadMore();
			}
		});

		setMedia(media);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		swipeRefresher = new SwipeRefreshLayout(inflater.getContext());
		swipeRefresher.setOnRefreshListener(() -> loadData(comment, comment, 0));

		var parentLayout = new LinearLayoutCompat(inflater.getContext());
		parentLayout.setOrientation(LinearLayoutCompat.VERTICAL);
		swipeRefresher.addView(parentLayout);

		recycler = new RecyclerView(inflater.getContext());
		recycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
		recycler.setClipToPadding(false);

		ViewUtil.setOnApplyUiInsetsListener(recycler, insets -> {
			var padding = dpPx(8);

			setTopPadding(recycler, insets.top + padding);
			setRightPadding(recycler, insets.right);
			setBottomPadding(recycler, padding);
		}, container);

		recycler.setAdapter(concatAdapter);
		parentLayout.addView(recycler, createLinearParams(MATCH_PARENT, 0, 1));

		var isSending = new AtomicBoolean();
		sendBinding = WidgetCommentSendBinding.inflate(inflater, parentLayout, true);
		sendBinding.getRoot().setVisibility(View.GONE);

		sendBinding.cancelEditing.setOnClickListener(v -> {
			sendBinding.editing.setVisibility(View.GONE);
			sendBinding.input.setText(null);
		});

		sendBinding.sendButton.setOnClickListener(v -> {
			if(isSending.getAndSet(true)) return;

			var text = sendBinding.input.getText().toString();
			var wasCurrentComment = comment;

			if(text.isBlank()) {
				isSending.set(false);
				return;
			}

			var newComment = new CatalogComment();
			newComment.text = text;

			sendBinding.loadingIndicator.setVisibility(View.VISIBLE);
			sendBinding.sendButton.setVisibility(View.INVISIBLE);

			if(editedComment != null) {
				selectedProvider.editComment(editedComment, newComment, new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(CatalogComment comment) {
						if(getContext() == null) return;
						if(wasCurrentComment != MediaCommentsFragment.this.comment) return;

						runOnUiThread(() -> {
							if(editedComment == wasCurrentComment) {
								//TODO: Modify all other stuff
								wasCurrentComment.text = newComment.text;

								commentsAdapter.setData(wasCurrentComment);
								commentsAdapter.notifyItemChanged(0);
							} else {
								loadData(MediaCommentsFragment.this.comment,
										MediaCommentsFragment.this.comment, 0);
							}

							sendBinding.loadingIndicator.setVisibility(View.GONE);
							sendBinding.sendButton.setVisibility(View.VISIBLE);
							sendBinding.input.setText(null);
							isSending.set(false);
						}, recycler);
					}

					@Override
					public void onFailure(Throwable e) {
						if(getContext() == null) return;
						if(wasCurrentComment != MediaCommentsFragment.this.comment) return;

						Log.e(TAG, "Failed to post a comment", e);

						runOnUiThread(() -> {
							CrashHandler.showErrorDialog(requireContext(),
									"Failed to post a comment! :(", e);

							sendBinding.loadingIndicator.setVisibility(View.GONE);
							sendBinding.sendButton.setVisibility(View.VISIBLE);
							isSending.set(false);
						}, recycler);
					}
				});
			} else {
				var request = new PostMediaCommentRequest()
						.setComment(newComment)
						.setParentComment(comment);

				getEpisodeNumber: try {
					var binding = headerAdapter.getBinding();

					if(binding != null) {
						var texta = binding.episodeDropdown.getText().toString();
						if(texta.isBlank()) break getEpisodeNumber;

						var number = Float.parseFloat(texta);
						request.setEpisode(new CatalogEpisode(number));
					}
				} catch(NumberFormatException e) {
					headerAdapter.getBinding().episodeWrapper.setError(e.getMessage());
				}

				selectedProvider.postMediaComment(request, new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(CatalogComment comment) {
						if(getContext() == null) return;
						if(wasCurrentComment != MediaCommentsFragment.this.comment) return;

						runOnUiThread(() -> {
							/* So apparently people wanna to see all comments
							even after you did post a new one. Weird... */

							loadData(MediaCommentsFragment.this.comment,
									MediaCommentsFragment.this.comment, 0);

							sendBinding.loadingIndicator.setVisibility(View.GONE);
							sendBinding.sendButton.setVisibility(View.VISIBLE);
							sendBinding.input.setText(null);
							isSending.set(false);
						}, recycler);
					}

					@Override
					public void onFailure(Throwable e) {
						if(getContext() == null) return;
						if(wasCurrentComment != MediaCommentsFragment.this.comment) return;

						Log.e(TAG, "Failed to post a comment", e);

						runOnUiThread(() -> {
							CrashHandler.showErrorDialog(requireContext(),
									"Failed to post a comment! :(", e);

							sendBinding.loadingIndicator.setVisibility(View.GONE);
							sendBinding.sendButton.setVisibility(View.VISIBLE);
							isSending.set(false);
						}, recycler);
					}
				});
			}
		});

		ViewUtil.setOnApplyUiInsetsListener(sendBinding.getRoot(), insets ->
				setRightMargin(sendBinding.getRoot(), insets.right), parentLayout);

		return swipeRefresher;
	}

	private void tryLoadMore() {
		if(media == null || comment == null) return;
		var page = pages.get(comment);

		if(page == null) {
			throw new NullPointerException("Page not found!");
		}

		if(!isLoading && page != LAST_PAGE) {
			var lastIndex = comment.size() - 1;

			if(recycler.getLayoutManager() instanceof LinearLayoutManager manager
					&& manager.findLastVisibleItemPosition() >= lastIndex) {
				pages.put(comment, page + 1);
				loadData(comment, comment, page + 1);
			}
		}
	}

	private class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
		private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
		private CatalogComment root;

		public CommentsAdapter() {
			setHasStableIds(true);
		}

		@SuppressLint("NotifyDataSetChanged")
		public void setData(@Nullable CatalogComment root) {
			this.root = root;
			idGenerator.clear();

			if(root != null) {
				root.visualId = idGenerator.getLong();

				for(var item : root.items) {
					item.visualId = idGenerator.getLong();
				}
			}

			notifyDataSetChanged();
		}

		public void addData(@Nullable CatalogComment root) {
			if(root == null) return;

			for(var item : root.items) {
				item.visualId = idGenerator.getLong();
			}

			var wasSize = comment.size();
			this.root.addAll(root.items);
			notifyItemRangeInserted(wasSize, root.size());
		}

		@NonNull
		@Override
		public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = WidgetCommentBinding.inflate(inflater, parent, false);
			var holder = new CommentViewHolder(binding);

			binding.getRoot().setOnClickListener(v -> {
				var comment = holder.getComment();
				if(comment == MediaCommentsFragment.this.comment) return;

				loadData(comment, null, 0);
			});

			return holder;
		}

		@Override
		public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
			CatalogComment item;

			if(root.text != null) {
				item = position == 0 ? root :
						root.items.get(position - 1);
			} else {
				item = root.items.get(position);
			}

			holder.bind(item);
		}

		@Override
		public long getItemId(int position) {
			CatalogComment item;

			if(root.text != null) {
				item = position == 0 ? root :
					root.items.get(position - 1);
			} else {
				item = root.items.get(position);
			}

			return item.visualId;
		}

		@Override
		public int getItemCount() {
			if(root == null) return 0;

			if(root.text != null) {
				return root.items.size() + 1;
			}

			return root.items.size();
		}
	}

	private class CommentViewHolder extends RecyclerView.ViewHolder {
		private final WidgetCommentBinding binding;
		private CatalogComment comment;

		public CommentViewHolder(@NonNull WidgetCommentBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.deleteButton.setOnClickListener(v -> {
				var comment = getComment();
				if(comment == null) return;

				new DialogBuilder(requireContext())
						.setTitle("Delete the comment?")
						.setMessage("You'll be unable to undo this action later.")
						.setPositiveButton(R.string.confirm, dialog -> {
							selectedProvider.deleteComment(comment, new ExtensionProvider.ResponseCallback<>() {

								@Override
								public void onSuccess(Boolean success) {
									var context = getContext();
									if(context == null) return;

									if(!success) {
										toast("Failed to delete comment");
										return;
									}

									loadData(MediaCommentsFragment.this.comment, MediaCommentsFragment.this.comment, 0);
								}

								@Override
								public void onFailure(Throwable e) {
									var context = getContext();
									if(context == null) return;

									CrashHandler.showErrorDialog(context, e);
								}
							});

							dialog.dismiss();
						})
						.setCancelButton(R.string.cancel, DialogBuilder::dismiss)
						.show();
			});

			binding.editButton.setOnClickListener(v -> {
				editedComment = getComment();

				sendBinding.input.setText(editedComment.text);
				sendBinding.editing.setVisibility(View.VISIBLE);
			});

			binding.likeButton.setOnClickListener(v -> {
				var comment = getComment();
				if(comment == null) return;

				comment.voteState = comment.voteState == CatalogComment.VOTE_STATE_LIKED ?
						CatalogComment.VOTE_STATE_NONE : CatalogComment.VOTE_STATE_LIKED;

				updateVotesState(true);
			});

			binding.dislikeButton.setOnClickListener(v -> {
				var comment = getComment();
				if(comment == null) return;

				comment.voteState = comment.voteState == CatalogComment.VOTE_STATE_DISLIKED ?
						CatalogComment.VOTE_STATE_NONE : CatalogComment.VOTE_STATE_DISLIKED;

				updateVotesState(true);
			});
		}

		private void updateVotesState(boolean sendRequest) {
			binding.likeIcon.setImageResource(comment.voteState == CatalogComment.VOTE_STATE_LIKED
					? R.drawable.ic_like_filled : R.drawable.ic_like_outlined);

			binding.dislikeIcon.setImageResource(comment.voteState == CatalogComment.VOTE_STATE_DISLIKED
					? R.drawable.ic_dislike_filled : R.drawable.ic_dislike_outlined);

			binding.likesCount.setText(comment.likes == 0 ? "" : String.valueOf(comment.likes
					+ (comment.voteState == CatalogComment.VOTE_STATE_LIKED ? 1 : 0)));

			binding.dislikesCount.setText(comment.dislikes == 0 ? "" : String.valueOf(comment.dislikes
					+ (comment.voteState == CatalogComment.VOTE_STATE_DISLIKED ? 1 : 0)));

			binding.votesCount.setText((comment.votes == null || comment.votes == 0) ? ""
					: String.valueOf(comment.votes + comment.voteState));

			if(sendRequest) {
				selectedProvider.voteComment(comment, new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(CatalogComment updatedComment) {}

					@Override
					public void onFailure(Throwable e) {
						var context = getContext();
						if(context == null) return;

						runOnUiThread(() -> {
							Log.e(TAG, "Failed to vote comment", e);
							CrashHandler.showErrorDialog(context, e);
						});
					}
				});
			}
		}

		public CatalogComment getComment() {
			return comment;
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogComment comment) {
			this.comment = comment;
			String date = null;

			var isRoot = comment == MediaCommentsFragment.this.comment;

			binding.getRoot().setBackgroundColor(isRoot ? (resolveAttrColor(requireContext(),
					com.google.android.material.R.attr.colorOnTertiary) - 0xBB000000) : 0);

			if(comment.date != null) {
				try {
					var time = StringUtils.parseDate(comment.date).getTime();
					var now = System.currentTimeMillis();

					date = DateUtils.getRelativeTimeSpanString(
							time, now, DateUtils.SECOND_IN_MILLIS).toString();
				} catch(InvalidSyntaxException e) {
					Log.e("MediaCommentsFragment", "Failed to parse comment date!", e);
					date = comment.date;
				}
			}

			if(date == null) binding.name.setText(comment.authorName);
			else binding.name.setText(comment.authorName + " • " + date);

			binding.message.setText(comment.text);
			binding.commentsCount.setText(comment.comments == 0 ? "" : String.valueOf(comment.comments));

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

			if(comment.comments == CatalogComment.DISABLED) {
				binding.commentIcon.setVisibility(View.GONE);
				binding.commentsCount.setVisibility(View.GONE);
				binding.commentButton.setVisibility(View.GONE);
			} else if(comment.comments == CatalogComment.HIDDEN) {
				binding.commentIcon.setVisibility(View.VISIBLE);
				binding.commentsCount.setVisibility(View.GONE);
				binding.commentButton.setVisibility(View.VISIBLE);
			} else {
				binding.commentIcon.setVisibility(View.VISIBLE);
				binding.commentsCount.setVisibility(View.VISIBLE);
				binding.commentButton.setVisibility(View.VISIBLE);
			}

			if(comment.votes != null) binding.votesCount.setVisibility(View.VISIBLE);
			else binding.votesCount.setVisibility(View.GONE);

			binding.editButton.setVisibility(comment.isEditable ? View.VISIBLE : View.GONE);
			binding.deleteButton.setVisibility(comment.isDeletable ? View.VISIBLE : View.GONE);

			Glide.with(binding.icon)
					.clear(binding.icon);

			Glide.with(binding.icon)
					.load(comment.authorAvatar)
					.transition(DrawableTransitionOptions.withCrossFade())
					.into(binding.icon);

			updateVotesState(false);
		}
	}
}