package com.mrboomdev.awery.ui.dialogs

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mrboomdev.awery.databinding.PopupMediaActionsBinding
import com.mrboomdev.awery.extensions.data.CatalogMedia
import com.mrboomdev.awery.ui.activity.MediaActivity
import com.mrboomdev.awery.util.MediaUtils
import com.mrboomdev.awery.util.extensions.inflater
import com.mrboomdev.awery.util.extensions.openActivity
import com.mrboomdev.awery.util.extensions.share

class MediaActionsDialog(val media: CatalogMedia) : BasePanelDialog() {
    var updateCallback: (() -> Unit)? = null

    override fun getView(context: Context): View {
        val binding = PopupMediaActionsBinding.inflate(context.inflater)
        if(media.url == null) binding.share.visibility = View.GONE
        binding.title.text = media.title

        binding.share.setOnClickListener { media.share(context) }
        binding.bookmark.setOnClickListener { MediaBookmarkDialog(media).show(context) }
        binding.close.setOnClickListener { dismiss() }

        binding.play.setOnClickListener {
            media.openActivity(context, MediaActivity.EXTRA_ACTION_WATCH)
            dismiss()
        }

        binding.hide.setOnClickListener {
            MediaUtils.blacklistMedia(media, updateCallback)
            dismiss()
        }

        media.bestPoster.let {
            if(it == null) binding.poster.visibility = View.GONE
            else Glide.with(context)
                .load(it)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.poster)
        }

        return binding.root
    }
}