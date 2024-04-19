package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.databinding.ItemListDropdownBinding;
import com.mrboomdev.awery.databinding.LayoutTrackingOptionsBinding;
import com.mrboomdev.awery.databinding.MediaDetailsOverviewLayoutBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogTrackingOptions;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TranslationUtil;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.adapter.ArrayListAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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

		binding.details.title.setText(media.getTitle());
		binding.details.generalMeta.setText(generateGeneralMetaString(media));

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
			var html = Html.fromHtml(media.description, Html.FROM_HTML_MODE_COMPACT).toString();
			binding.details.description.setText(html.trim());
		} else {
			binding.details.description.setVisibility(View.GONE);
			binding.details.descriptionTitle.setVisibility(View.GONE);
		}

		if(media.tags.isEmpty()) {
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
				builder.append(media.duration).append("m ");
			} else {
				builder.append((media.duration / 60)).append("h ")
						.append((media.duration % 60)).append("m ");
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

		binding.details.tracking.setOnClickListener(v -> openTrackingDialog());

		setMedia(media);
		return binding.getRoot();
	}

	private void openTrackingDialog() {
		var mappedIds = new HashMap<String, ExtensionProvider>();

		var progress = new AtomicReference<CatalogMediaProgress>();
		var inflater = LayoutInflater.from(requireContext());

		var binding = LayoutTrackingOptionsBinding.inflate(inflater, null, false);
		binding.source.input.setText("Select a tracker", false);
		binding.title.input.setText("Select a title", false);

		var adapter = new ArrayListAdapter<String>((id, recycled, parent) -> {
			var item = mappedIds.get(id);

			if(item == null) {
				throw new IllegalStateException("Invalid provider id: " + id);
			}

			if(recycled == null) {
				var itemBinding = ItemListDropdownBinding.inflate(inflater, parent, false);
				recycled = itemBinding.getRoot();
			}

			TextView title = recycled.findViewById(R.id.title);
			ImageView icon = recycled.findViewById(R.id.icon);

			title.setText(item.getName());

			return recycled;
		});

		binding.source.input.setOnItemClickListener((parent, view, position, id) -> {
			var itemId = adapter.getItem(position);
			var item = mappedIds.get(itemId);

			if(item == null) {
				throw new IllegalStateException("Invalid provider id: " + itemId);
			}

			binding.source.input.setText(item.getName(), false);
			updateTrackingDialogState(binding, null, null);
		});

		binding.source.input.setAdapter(adapter);
		updateTrackingDialogState(binding, null, null);

		var sheet = new BottomSheetDialog(requireContext());
		sheet.setContentView(binding.getRoot());
		sheet.show();
		DialogUtils.fixDialog(sheet);

		new Thread(() -> {
			var progressDao = getDatabase().getMediaProgressDao();
			progress.set(progressDao.get(media.globalId));

			if(progress.get() == null) {
				progress.set(new CatalogMediaProgress(media.globalId));
				progressDao.insert(progress.get());
			}

			var availableSources = stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
					.map(ext -> ext.getProviders(ExtensionProvider.FEATURE_TRACK))
					.flatMap(AweryApp::stream)
					.toList();

			for(var provider : availableSources) {
				mappedIds.put(provider.getId(), provider);
			}

			runOnUiThread(() -> {
				if(availableSources.isEmpty()) {
					updateTrackingDialogState(binding, null,
							new ZeroResultsException("No trackable sources available", R.string.no_tracker_extensions));
				} else {
					var foundTracked = stream(availableSources)
							.filter(provider -> progress.get().trackers.contains(provider.getId()))
							.findFirst();

					var defaultTracked = foundTracked.isPresent()
							? foundTracked.get() : availableSources.get(0);

					binding.source.input.setText(defaultTracked.getName(), false);
					//TODO: Load animes list for titles
				}

				adapter.setItems(mappedIds.keySet());
			});
		}).start();
	}

	private void updateTrackingDialogState(
			LayoutTrackingOptionsBinding binding,
			CatalogTrackingOptions trackingOptions,
			Throwable throwable
	) {
		if(getContext() == null) return;

		if(trackingOptions == null) {
			binding.loadingState.getRoot().setVisibility(View.VISIBLE);

			if(throwable != null) {
				var description = new ExceptionDescriptor(throwable);

				binding.loadingState.title.setText(description.getTitle(requireContext()));
				binding.loadingState.message.setText(description.getMessage(requireContext()));

				binding.loadingState.progressBar.setVisibility(View.GONE);
				binding.loadingState.info.setVisibility(View.VISIBLE);
			} else {
				binding.loadingState.info.setVisibility(View.GONE);
				binding.loadingState.progressBar.setVisibility(View.VISIBLE);
			}
		} else {
			binding.loadingState.getRoot().setVisibility(View.GONE);
		}

		binding.progressIncrement.setOnClickListener(v -> {
			if(trackingOptions == null) return;

			trackingOptions.progress++;
			binding.progress.setText(String.valueOf(trackingOptions.progress));
		});

		binding.progress.setText(String.valueOf(trackingOptions != null ? trackingOptions.progress : 0));

		binding.progressWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_PROGRESS) ? View.VISIBLE : View.GONE);

		binding.statusWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_LISTS) ? View.VISIBLE : View.GONE);

		binding.isPrivateWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_PRIVATE) ? View.VISIBLE : View.GONE);

		binding.scoreWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_SCORE) ? View.VISIBLE : View.GONE);

		binding.dateWrapper.setVisibility(trackingOptions != null &&
				(trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_DATE_START)
						|| trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_DATE_END)) ? View.VISIBLE : View.GONE);
	}
}