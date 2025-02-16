package com.mrboomdev.awery.ui.mobile.dialogs

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mrboomdev.awery.app.App.Companion.share
import com.mrboomdev.awery.databinding.PopupMediaActionsBinding
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ui.mobile.screens.media.MediaActivity
import com.mrboomdev.awery.util.MediaUtils
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.inflater

class MediaActionsDialog(val media: CatalogMedia) : BasePanelDialog() {
    var updateCallback: (() -> Unit)? = null

    override fun getView(context: Context) = with(context) {
        val binding = PopupMediaActionsBinding.inflate(context.inflater)
        if(media.extras[CatalogMedia.EXTRA_SHARE] == null) binding.share.visibility = View.GONE
        binding.title.text = media.title

        binding.share.setOnClickListener { media.extras[CatalogMedia.EXTRA_SHARE]?.let { share(it) } }

        binding.bookmark.setOnClickListener { MediaBookmarkDialog(media).show(context) }
        binding.close.setOnClickListener { dismiss() }

        binding.play.setOnClickListener {
            context.startActivity(buildIntent(MediaActivity::class,
                MediaActivity.Extras(media, MediaActivity.Action.WATCH)))

            dismiss()
        }

        binding.hide.setOnClickListener {
            MediaUtils.blacklistMedia(media, updateCallback)
            dismiss()
        }
        
        media.extras[CatalogMedia.EXTRA_POSTER].let {
            if(it == null) binding.poster.visibility = View.GONE
            else Glide.with(context)
                .load(it)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.poster)
        }

        return@with binding.root
    }
}