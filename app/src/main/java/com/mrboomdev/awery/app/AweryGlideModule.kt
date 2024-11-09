package com.mrboomdev.awery.app

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.mrboomdev.awery.util.io.HttpClient.client
import java.io.InputStream

@GlideModule
class AweryGlideModule : AppGlideModule() {
	override fun applyOptions(context: Context, builder: GlideBuilder) {
		super.applyOptions(context, builder)

		val diskCacheSizeBytes = 1024 * 1024 * 100 // 100 MiB
		builder.setDiskCache(InternalCacheDiskCacheFactory(context, "img", diskCacheSizeBytes.toLong()))
	}

	override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
		registry.replace(
			GlideUrl::class.java,
			InputStream::class.java,
			OkHttpUrlLoader.Factory(client)
		)

		super.registerComponents(context, glide, registry)
	}
}