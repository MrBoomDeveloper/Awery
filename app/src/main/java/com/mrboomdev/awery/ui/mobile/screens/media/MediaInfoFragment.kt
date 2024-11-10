package com.mrboomdev.awery.ui.mobile.screens.media

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.chip.Chip
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.getMarkwon
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.openUrl
import com.mrboomdev.awery.app.App.Companion.orientation
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLocales
import com.mrboomdev.awery.databinding.MediaDetailsOverviewLayoutBinding
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogTag
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.ui.mobile.screens.GalleryActivity
import com.mrboomdev.awery.ui.mobile.screens.media.MediaActivity.Companion.handleOptionsClick
import com.mrboomdev.awery.ui.mobile.screens.search.SearchActivity
import com.mrboomdev.awery.ui.mobile.dialogs.MediaBookmarkDialog
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.arrayOfNotNull
import com.mrboomdev.awery.util.extensions.bottomPadding
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.rightPadding
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.toCalendar
import com.mrboomdev.awery.util.extensions.toColorState
import com.mrboomdev.awery.util.extensions.topMargin
import com.mrboomdev.awery.util.extensions.topPadding
import com.mrboomdev.safeargsnext.SafeArgsIntent
import com.mrboomdev.safeargsnext.owner.SafeArgsFragment
import com.mrboomdev.safeargsnext.util.putSafeArgs
import com.mrboomdev.safeargsnext.util.rememberSafeArgs
import java.util.Calendar

class MediaInfoFragment: Fragment(), SafeArgsFragment<MediaInfoFragment.Args> {
	private lateinit var binding: MediaDetailsOverviewLayoutBinding

	data class Args(val media: CatalogMedia)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val media = rememberSafeArgs!!.media
		val type = media.type ?: CatalogMedia.Type.TV
		val title = media.title ?: "No title"
		val meta = generateGeneralMetaString(media)

		binding.details.play.setText(when(type) {
			CatalogMedia.Type.TV, CatalogMedia.Type.MOVIE -> R.string.watch
			CatalogMedia.Type.BOOK, CatalogMedia.Type.POST -> R.string.read
		})

		binding.details.play.icon = ContextCompat.getDrawable(requireContext(), when(type) {
			CatalogMedia.Type.TV, CatalogMedia.Type.MOVIE -> R.drawable.ic_play_filled
			CatalogMedia.Type.BOOK, CatalogMedia.Type.POST -> R.drawable.ic_round_import_contacts_24
		})

		if(meta.isBlank()) {
			binding.details.generalMeta.visibility = View.GONE
		}

		binding.details.title.text = title
		binding.details.generalMeta.text = meta

		val banner = if(orientation == Configuration.ORIENTATION_LANDSCAPE) media.banner else media.poster

		Glide.with(binding.root)
			.load(banner ?: media.poster)
			.transition(withCrossFade())
			.into(binding.banner)

		Glide.with(binding.root)
			.load(media.poster ?: banner)
			.transition(withCrossFade())
			.into(binding.poster)

		binding.posterWrapper.setOnClickListener {
			if(media.poster == null && media.banner == null) {
				return@setOnClickListener
			}

			startActivity(SafeArgsIntent(requireContext(), GalleryActivity::class).apply {
				binding.poster.transitionName = "poster"
				putSafeArgs(GalleryActivity.Extras(arrayOfNotNull(media.poster, media.banner)))
			}, makeSceneTransitionAnimation(requireActivity(), binding.poster, "poster").toBundle())
		}

		binding.details.play.setOnClickListener {
			(requireActivity() as MediaActivity).launchAction(MediaActivity.Action.WATCH)
		}

		binding.details.bookmark.setOnClickListener {
			MediaBookmarkDialog(media).show(requireContext())
		}

		if(media.description != null && media.description!!.isNotBlank()) {
			val description = getMarkwon(requireContext()).toMarkdown(media.description!!)

			if(description.toString().isNotBlank()) {
				binding.details.description.text = description
			} else {
				binding.details.description.visibility = View.GONE
				binding.details.descriptionTitle.visibility = View.GONE
			}
		} else {
			binding.details.description.visibility = View.GONE
			binding.details.descriptionTitle.visibility = View.GONE
		}

		if(media.tags == null || media.tags!!.isEmpty()) {
			if(media.genres == null) {
				binding.details.tagsTitle.visibility = View.GONE
				binding.details.tags.visibility = View.GONE
			} else {
				for(tag in media.genres!!) {
					addTagView(CatalogTag(tag))
				}
			}
		} else {
			val spoilers = HashSet<CatalogTag>()

			for(tag in media.tags!!) {
				if(tag.isSpoiler) {
					spoilers.add(tag)
					continue
				}

				addTagView(tag)
			}

			if(spoilers.isNotEmpty()) {
				val spoilerChip = Chip(requireContext())

				spoilerChip.chipBackgroundColor = requireContext().resolveAttrColor(
					com.google.android.material.R.attr.colorSecondaryContainer).toColorState()

				spoilerChip.setText(R.string.show_spoilers)
				binding.details.tags.addView(spoilerChip)

				spoilerChip.setOnClickListener {
					binding.details.tags.removeView(spoilerChip)

					for(tag in spoilers) {
						addTagView(tag)
					}
				}
			}
		}

		binding.details.browser.visibility = if(media.url != null) View.VISIBLE else View.GONE
	}

	private fun addTagView(tag: CatalogTag) {
		val chip = Chip(requireContext())
		chip.text = tag.name
		binding.details.tags.addView(chip)

		chip.setOnClickListener {
			startActivity(
				SearchActivity::class, args = SearchActivity.Extras(
				action = SearchActivity.Action.SEARCH_BY_TAG,
				queryTag = tag.name,
				sourceGlobalId = rememberSafeArgs!!.media.globalId
			))
		}

		chip.setOnLongClickListener {
			toast("New thing will appear here in future ;)")
			true
		}
	}

	private fun generateGeneralMetaString(media: CatalogMedia): String {
		val metas = mutableListOf<String>()

		if(media.episodesCount != null) {
			if(media.episodesCount == 1) metas.add(getString(R.string.episode))
			else metas.add(getString(R.string.episodes))
		}

		if(media.duration != null) {
			metas.add(media.duration!!.let {
				return@let if(it < 60) {
					"$it${getString(R.string.minute_short)}"
				} else {
					"${it / 60}${getString(R.string.hour_short)} " +
							"${it % 60}${getString(R.string.minute_short)}"
				}
			} + " " + getString(R.string.duration))
		}

		if(media.releaseDate != null) {
			metas.add(media.releaseDate!!.toCalendar()[Calendar.YEAR].toString())
		}

		if(media.country != null) {
			metas.add(AweryLocales.translateCountryName(requireContext(), media.country!!))
		}

		if(metas.size < 4 && media.status != null) {
			metas.add(getString(when(media.status!!) {
				CatalogMedia.Status.ONGOING -> R.string.status_releasing
				CatalogMedia.Status.COMPLETED -> R.string.status_finished
				CatalogMedia.Status.COMING_SOON -> R.string.status_not_yet_released
				CatalogMedia.Status.PAUSED -> R.string.status_hiatus
				CatalogMedia.Status.CANCELLED -> R.string.status_cancelled
			}))
		}

		if(metas.size < 4) {
			try {
				metas.add(ExtensionProvider.forGlobalId(media.globalId).name)
			} catch(_: ExtensionNotInstalledException) {}
		}

		return metas.joinToString("  •  ")
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = MediaDetailsOverviewLayoutBinding.inflate(inflater, container, false)

		if(orientation == Configuration.ORIENTATION_PORTRAIT) {
			binding.posterWrapper.applyInsets(UI_INSETS, { view, insets ->
				view.topMargin = insets.top + dpPx(24f)
				true
			})
		}

		if(binding.back != null) {
			binding.back!!.setOnClickListener { requireActivity().finish() }

			binding.back!!.applyInsets(UI_INSETS, { view, insets ->
				view.topMargin = insets.top + dpPx(16f)
				true
			})
		}

		val options = binding.options ?: binding.details.options
		options!!.setOnClickListener { v -> handleOptionsClick(v!!, rememberSafeArgs!!.media) }

		options.applyInsets(UI_INSETS, { view, insets ->
			view.topMargin = if(isLandscape) 0
			else insets.top + dpPx(16f)
			true
		})

		binding.detailsScroller?.applyInsets(UI_INSETS, { view, insets ->
			view.topPadding = insets.top + dpPx(8f)
			view.bottomPadding = insets.bottom + dpPx(8f)
			view.rightPadding = insets.right + (dpPx(16f))
			false
		})

		binding.details.tracking.setOnClickListener {
			toast("Will be returned in the near future")
		}

		binding.details.browser.setOnClickListener {
			openUrl(requireContext(), rememberSafeArgs!!.media.url!!, true)
		}

		return binding.root
	}
}