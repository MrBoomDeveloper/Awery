package com.mrboomdev.awery.util.extensions

import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat.IntentBuilder
import com.mrboomdev.awery.extensions.data.CatalogMedia
import com.mrboomdev.awery.ui.activity.MediaActivity
import com.mrboomdev.awery.ui.activity.MediaActivity.Action

fun CatalogMedia.openActivity(context: Context, @Action action: String? = null) {
    val intent = Intent(context, MediaActivity::class.java)
    intent.putExtra(MediaActivity.EXTRA_MEDIA, this)
    intent.putExtra(MediaActivity.EXTRA_ACTION, action)
    context.startActivity(intent)
}

fun CatalogMedia.share(context: Context) {
    IntentBuilder(context)
        .setType("text/plain")
        .setText(url)
        .startChooser()
}