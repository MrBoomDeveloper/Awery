package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.App.getMarkwon;
import static com.mrboomdev.awery.app.App.getOrientation;
import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.App.openUrl;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.MediaDetailsOverviewLayoutBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.ui.activity.GalleryActivity;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.ui.activity.search.SearchActivity;
import com.mrboomdev.awery.ui.dialogs.MediaBookmarkDialog;
import com.mrboomdev.awery.util.TranslationUtil;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashSet;

import java9.util.Objects;

public class MediaInfoFragment extends Fragment {
	private static final String TAG = "MediaInfoFragment";
	private WeakReference<Drawable> cachedPoster;
	private MediaDetailsOverviewLayoutBinding binding;
	private CatalogMedia media;

	public MediaInfoFragment(CatalogMedia media) {
		this.media = media;

		var bundle = new Bundle();
		bundle.putSerializable("media", media);
		setArguments(bundle);
	}

	/**
	 * DO NOT CALL THIS CONSTRUCTOR DIRECTLY!
	 * @author MrBoomDev
	 */
	public MediaInfoFragment() {
		this(null);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		if(media == null) {
			media = requireArgument(this, "media");
		}

		var type = Objects.requireNonNullElse(media.type, CatalogMedia.MediaType.TV);
		var title = Objects.requireNonNullElse(media.getTitle(), "No title");
		var meta = generateGeneralMetaString(media);

		binding.details.play.setText(switch(type) {
			case TV, MOVIE -> R.string.watch;
			case BOOK, POST -> R.string.read;
		});

		binding.details.play.setIcon(ContextCompat.getDrawable(requireContext(), switch(type) {
			case TV, MOVIE -> R.drawable.ic_play_filled;
			case BOOK, POST -> R.drawable.ic_round_import_contacts_24;
		}));

		if(meta.isBlank()) {
			binding.details.generalMeta.setVisibility(View.GONE);
		}

		binding.details.title.setText(title);
		binding.details.generalMeta.setText(meta);

		var banner = getOrientation() == Configuration.ORIENTATION_LANDSCAPE
				? media.getBestBanner() : media.getBestPoster();

		Glide.with(binding.getRoot())
				.load(banner)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.banner);

		Glide.with(binding.getRoot())
				.load(media.getBestPoster())
				.transition(DrawableTransitionOptions.withCrossFade())
				.addListener(new RequestListener<>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
						return false;
					}

					@Override
					public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
						cachedPoster = new WeakReference<>(resource);
						return false;
					}
				}).into(binding.poster);

		binding.posterWrapper.setOnClickListener(v -> {
			binding.poster.setTransitionName("poster");

			var intent = new Intent(requireContext(), GalleryActivity.class);
			intent.putExtra(GalleryActivity.EXTRA_URLS, new String[] { media.getBestPoster() });
			startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(
					requireActivity(), binding.poster, "poster").toBundle());
		});

		binding.details.play.setOnClickListener(v -> {
			if(requireActivity() instanceof MediaActivity activity) activity.launchAction("watch");
			else throw new IllegalStateException("Activity is not an instance of MediaActivity!");
		});

		binding.details.bookmark.setOnClickListener(v ->
				new MediaBookmarkDialog(media).show(requireContext()));

		if(media.description != null && !media.description.isBlank()) {
			var description = getMarkwon(requireContext()).toMarkdown(media.description);

			if(!description.toString().isBlank()) {
				binding.details.description.setText(description);
			} else {
				binding.details.description.setVisibility(View.GONE);
				binding.details.descriptionTitle.setVisibility(View.GONE);
			}
		} else {
			binding.details.description.setVisibility(View.GONE);
			binding.details.descriptionTitle.setVisibility(View.GONE);
		}

		if(media.tags == null || media.tags.isEmpty()) {
			binding.details.tagsTitle.setVisibility(View.GONE);
			binding.details.tags.setVisibility(View.GONE);
		} else {
			var spoilers = new HashSet<CatalogTag>();

			for(var tag : media.tags) {
				if(tag.isSpoiler()) {
					spoilers.add(tag);
					continue;
				}

				addTagView(tag);
			}

			if(!spoilers.isEmpty()) {
				var spoilerChip = new Chip(requireContext());

				spoilerChip.setChipBackgroundColor(ColorStateList.valueOf(resolveAttrColor(
						requireContext(), com.google.android.material.R.attr.colorSecondaryContainer)));

				spoilerChip.setText("Show spoilers");
				binding.details.tags.addView(spoilerChip);

				spoilerChip.setOnClickListener(v -> {
					binding.details.tags.removeView(spoilerChip);

					for(var tag : spoilers) {
						addTagView(tag);
					}
				});
			}
		}

		binding.details.browser.setVisibility(media.url != null ? View.VISIBLE : View.GONE);
	}

	private void addTagView(@NonNull CatalogTag tag) {
		var chip = new Chip(requireContext());
		chip.setText(tag.getName());
		binding.details.tags.addView(chip);

		chip.setOnClickListener(v -> {
			var intent = new Intent(requireContext(), SearchActivity.class);
			intent.setAction(SearchActivity.ACTION_SEARCH_BY_TAG);
			intent.putExtra(SearchActivity.EXTRA_TAG, tag.getName());
			intent.putExtra(SearchActivity.EXTRA_GLOBAL_PROVIDER_ID, media.globalId);
			startActivity(intent);
		});

		chip.setOnLongClickListener(v -> {
			toast("New thing will appear here in future ;)");
			return true;
		});
	}

	@NonNull
	private String generateGeneralMetaString(@NonNull CatalogMedia media) {
		var builder = new StringBuilder();

		if(media.episodesCount != null) {
			builder.append(media.episodesCount).append(" ");

			if(media.episodesCount == 1) builder.append(getString(R.string.episode));
			else builder.append(getString(R.string.episodes));
		}

		if(media.duration != null) {
			if(builder.length() > 0) builder.append(" • ");

			if(media.duration < 60) {
				builder.append(media.duration).append(getString(R.string.minute_short)).append(" ");
			} else {
				builder.append((media.duration / 60)).append(getString(R.string.hour_short)).append(" ")
						.append((media.duration % 60)).append(getString(R.string.minute_short)).append(" ");
			}

			builder.append(getString(R.string.duration));
		}

		if(media.releaseDate != null) {
			if(builder.length() > 0) builder.append(" • ");
			builder.append(media.releaseDate.get(Calendar.YEAR));
		}

		if(media.country != null) {
			if(builder.length() > 0) builder.append(" • ");
			builder.append(TranslationUtil.getTranslatedCountryName(requireContext(), media.country));
		}

		return builder.toString();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = MediaDetailsOverviewLayoutBinding.inflate(inflater, container, false);

		if(getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			setOnApplyUiInsetsListener(binding.posterWrapper, insets -> {
				setTopMargin(binding.posterWrapper, insets.top + dpPx(binding.posterWrapper, 24));
				return true;
			});
		}

		if(binding.back != null) {
			binding.back.setOnClickListener(v -> requireActivity().finish());

			setOnApplyUiInsetsListener(binding.back, insets -> {
				setTopMargin(binding.back, insets.top + dpPx(binding.back, 16));
				return true;
			});
		}

		var options = Objects.requireNonNullElse(binding.options, binding.details.options);
		options.setOnClickListener(v -> MediaActivity.handleOptionsClick(v, media));

		setOnApplyUiInsetsListener(options, insets -> {
			if(!isLandscape(requireContext())) {
				setTopMargin(options, insets.top + dpPx(options, 16));
			} else {
				setTopMargin(options, 0);
			}

			return true;
		});

		if(binding.detailsScroller != null) setOnApplyUiInsetsListener(binding.detailsScroller, insets -> {
			var margin = dpPx(binding.detailsScroller, 8);

			setTopPadding(binding.detailsScroller, insets.top + margin);
			setBottomPadding(binding.detailsScroller, insets.bottom + margin);
			setRightPadding(binding.detailsScroller, insets.right + (margin * 2));

			return false;
		});

		/*binding.details.tracking.setOnClickListener(v -> TrackingSheet.create(
				requireContext(), getChildFragmentManager(), media).show());*/
		binding.details.tracking.setVisibility(View.GONE);

		binding.details.browser.setOnClickListener(v ->
				openUrl(requireContext(), media.url, true));

		return binding.getRoot();
	}
}