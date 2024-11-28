package com.mrboomdev.awery.ui.mobile.screens.catalog.feeds

import android.view.View
import android.view.ViewGroup
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.navigationStyle
import com.mrboomdev.awery.app.App.Companion.openUrl
import com.mrboomdev.awery.databinding.FeedFailedBinding
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException
import com.mrboomdev.awery.util.exceptions.explain
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.context
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.inflater
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.rightMargin
import com.mrboomdev.awery.util.extensions.setHorizontalMargin

class FailedFeedViewHolder private constructor(
	private val binding: FeedFailedBinding, parent: ViewGroup
) : FeedViewHolder(binding.root) {

	init {
		binding.header.setOnClickListener { binding.expand.performClick() }

		binding.header.applyInsets(UI_INSETS, { view, insets ->
			if(isLandscape) {
				view.rightMargin = insets.right + view.dpPx(16f)
				view.leftMargin = view.dpPx(16f) +
						(if(navigationStyle != AwerySettings.NavigationStyle_Values.MATERIAL) insets.left else 0)
			} else {
				view.setHorizontalMargin(0)
			}
			true
		}, parent)
	}

	override fun bind(feed: Feed) {
		binding.title.text = feed.sourceFeed.title

		if(feed.reloadCallback != null && !feed.isLoading) {
			binding.expand.setImageResource(R.drawable.ic_refresh)
			binding.expand.visibility = View.VISIBLE
			binding.header.isClickable = true
			binding.expand.setOnClickListener { feed.reloadCallback!!.run() }

			try {
				val source = ExtensionProvider.forGlobalId(
					feed.sourceFeed.sourceManager,
					feed.sourceFeed.extensionId,
					feed.sourceFeed.sourceId)

				if(source.previewUrl != null) {
					binding.browse.visibility = View.VISIBLE
					binding.browse.setOnClickListener {
						openUrl(binding.context, source.previewUrl, true)
					}
				} else {
					binding.browse.visibility = View.GONE
				}
			} catch(_: ExtensionNotInstalledException) {
				binding.browse.visibility = View.GONE
			}
		} else {
			binding.header.isClickable = false
			binding.expand.visibility = View.GONE
			binding.browse.visibility = View.GONE
			binding.expand.setOnClickListener(null)
		}

		if(feed.throwable != null) {
			binding.errorMessage.text = feed.throwable!!.explain().print()
		} else {
			binding.errorMessage.text = binding.context.getString(R.string.nothing_found)
		}

		if(feed.isLoading) {
			binding.errorMessage.visibility = View.GONE
			binding.progressbar.visibility = View.VISIBLE
		} else {
			binding.errorMessage.visibility = View.VISIBLE
			binding.progressbar.visibility = View.GONE
		}
	}

	companion object {
		fun create(parent: ViewGroup) = FailedFeedViewHolder(FeedFailedBinding.inflate(
			parent.context.inflater, parent, false), parent)
	}
}