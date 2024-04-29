package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.databinding.MediaDetailsOverviewLayoutBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.ui.popup.sheet.TrackingSheet;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TranslationUtil;

import java.io.IOException;
import java.util.Calendar;

import java9.util.Objects;

public class MediaInfoFragment extends Fragment {
	private static final String TAG = "MediaInfoFragment";
	private MediaDetailsOverviewLayoutBinding binding;
	private CatalogMedia media;

	public MediaInfoFragment(CatalogMedia media) {
		setMedia(media);
	}

	public MediaInfoFragment() {
		this(null);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("media", media.toString());
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			try {
				var mediaJson = savedInstanceState.getString("media");
				if(mediaJson == null) return;

				setMedia(Parser.fromString(CatalogMedia.class, mediaJson));
			} catch(IOException e) {
				Log.e(TAG, "Failed to restore media!", e);
				AweryApp.toast("Failed to restore media!", 1);
			}
		}
	}

	@SuppressLint("SetTextI18n")
	public void setMedia(CatalogMedia media) {
		if(media == null) return;
		this.media = media;
		if(binding == null) return;

		var title = Objects.requireNonNullElse(media.getTitle(), "No title");
		var meta = generateGeneralMetaString(media);

		if(meta.isBlank()) {
			binding.details.generalMeta.setVisibility(View.GONE);
		}

		binding.details.title.setText(title);
		binding.details.generalMeta.setText(meta);

		var banner = AweryApp.getOrientation() == Configuration.ORIENTATION_LANDSCAPE
				? media.getBestBanner() : media.getBestPoster();

		Glide.with(binding.getRoot())
				.load(banner)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.banner);

		Glide.with(binding.getRoot())
				.load(media.poster.extraLarge)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.poster);

		binding.details.play.setOnClickListener(v -> {
			if(requireActivity() instanceof MediaActivity activity) activity.launchAction("watch");
			else throw new IllegalStateException("Activity is not an instance of MediaActivity!");
		});

		binding.details.bookmark.setOnClickListener(v -> MediaUtils.openMediaBookmarkMenu(requireContext(), media));

		if(media.description != null) {
			var html = Html.fromHtml(media.description, Html.FROM_HTML_MODE_COMPACT);
			binding.details.description.setText(html.toString().trim());
		} else {
			binding.details.description.setVisibility(View.GONE);
			binding.details.descriptionTitle.setVisibility(View.GONE);
		}

		if(media.tags == null || media.tags.isEmpty()) {
			binding.details.tagsTitle.setVisibility(View.GONE);
			binding.details.tags.setVisibility(View.GONE);
		} else {
			for(var tag : media.tags) {
				var chip = new Chip(requireContext());
				chip.setText(tag.getName());
				binding.details.tags.addView(chip);
			}
		}
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

		if(AweryApp.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			setOnApplyUiInsetsListener(binding.posterWrapper, insets ->
					setTopMargin(binding.posterWrapper, insets.top + dpPx(24)));
		}

		if(binding.back != null) {
			binding.back.setOnClickListener(v -> requireActivity().finish());
			setOnApplyUiInsetsListener(binding.back, insets -> setTopMargin(binding.back, insets.top + dpPx(16)));
		}

		var options = binding.options != null ? binding.options : binding.details.options;
		if(options == null) throw new NullPointerException("Options cannot be null!");

		options.setOnClickListener(v -> MediaActivity.handleOptionsClick(v, media));
		setOnApplyUiInsetsListener(options, insets -> setTopMargin(options, insets.top + dpPx(16)));

		if(binding.detailsScroller != null) setOnApplyUiInsetsListener(binding.detailsScroller, insets -> {
			var margin = dpPx(8);

			setTopPadding(binding.detailsScroller, insets.top + margin);
			setBottomPadding(binding.detailsScroller, insets.bottom + margin);
			setRightPadding(binding.detailsScroller, insets.right + (margin * 2));
		});

		binding.details.tracking.setOnClickListener(v -> TrackingSheet.create(
				requireContext(), getChildFragmentManager(), media).show());

		setMedia(media);
		return binding.getRoot();
	}
}