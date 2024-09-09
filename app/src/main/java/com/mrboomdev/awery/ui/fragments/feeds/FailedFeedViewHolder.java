package com.mrboomdev.awery.ui.fragments.feeds;

import static com.mrboomdev.awery.app.App.getNavigationStyle;
import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.Lifecycle.getContext;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.FeedFailedBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;

import org.jetbrains.annotations.Contract;

public class FailedFeedViewHolder extends FeedViewHolder {
	private final FeedFailedBinding binding;

	@NonNull
	@Contract("_ -> new")
	public static FailedFeedViewHolder create(ViewGroup parent) {
		return new FailedFeedViewHolder(FeedFailedBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false), parent);
	}

	private FailedFeedViewHolder(@NonNull FeedFailedBinding binding, ViewGroup parent) {
		super(binding.getRoot());
		this.binding = binding;

		binding.header.setOnClickListener(v -> binding.expand.performClick());

		setOnApplyUiInsetsListener(binding.header, insets -> {
			if(isLandscape()) {
				setLeftMargin(binding.header, dpPx(binding.header, 16) +
						(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL ? insets.left : 0));

				setRightMargin(binding.header, insets.right + dpPx(binding.header, 16));
			} else {
				setHorizontalMargin(binding.header, 0);
			}

			return true;
		}, parent);
	}

	@Override
	public void bind(@NonNull Feed feed) {
		binding.title.setText(feed.sourceFeed.title);

		if(feed.getReloadCallback() != null && !feed.isLoading) {
			binding.expand.setImageResource(R.drawable.ic_refresh);
			binding.expand.setVisibility(View.VISIBLE);
			binding.header.setClickable(true);

			binding.expand.setOnClickListener(v ->
					feed.getReloadCallback().run());
		} else {
			binding.header.setClickable(false);
			binding.expand.setVisibility(View.GONE);
			binding.expand.setOnClickListener(null);
		}

		if(feed.getThrowable() != null) {
			binding.errorMessage.setText(ExceptionDescriptor.print(
					ExceptionDescriptor.unwrap(feed.getThrowable()), getContext(binding)));
		} else {
			binding.errorMessage.setText(getContext(binding).getString(R.string.nothing_found));
		}

		if(feed.isLoading) {
			binding.errorMessage.setVisibility(View.GONE);
			binding.progressbar.setVisibility(View.VISIBLE);
		} else {
			binding.errorMessage.setVisibility(View.VISIBLE);
			binding.progressbar.setVisibility(View.GONE);
		}
	}
}