package com.mrboomdev.awery.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.mrboomdev.awery.util.io.HttpClient;

import java.io.InputStream;

@GlideModule
public class AweryGlideModule extends AppGlideModule {

	@Override
	public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
		super.applyOptions(context, builder);

		var diskCacheSizeBytes = 1024 * 1024 * 100; // 100 MiB
		builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "img", diskCacheSizeBytes));
	}

	@Override
	public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
		registry.replace(
				GlideUrl.class,
				InputStream.class,
				new OkHttpUrlLoader.Factory(HttpClient.getClient())
        );

		super.registerComponents(context, glide, registry);
	}
}