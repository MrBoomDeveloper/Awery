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
import com.mrboomdev.awery.util.extensions.inflater
import com.mrboomdev.awery.util.extensions.startActivity

class MediaActionsDialog(val media: CatalogMedia) : BasePanelDialog() {
    var updateCallback: (() -> Unit)? = null

    override fun getView(context: Context): View {
        val binding = PopupMediaActionsBinding.inflate(context.inflater)
        if(media.url == null) binding.share.visibility = View.GONE
        binding.title.text = media.title

        binding.share.setOnClickListener { media.url?.let { share(it) } }

        binding.bookmark.setOnClickListener { MediaBookmarkDialog(media).show(context) }
        binding.close.setOnClickListener { dismiss() }

        binding.play.setOnClickListener {
            context.startActivity(
				MediaActivity::class, MediaActivity.Extras(
                media = media, action = MediaActivity.Action.WATCH
            ))

            dismiss()
        }

        binding.hide.setOnClickListener {
            MediaUtils.blacklistMedia(media, updateCallback)
            dismiss()
        }

        media.poster.let {
            if(it == null) binding.poster.visibility = View.GONE
            else Glide.with(context)
                .load(it)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.poster)
        }

        return binding.root
    }
}