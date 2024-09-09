package com.mrboomdev.awery.app.data;

import static com.mrboomdev.awery.app.App.getResourceId;
import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.ext.data.Image;
import com.mrboomdev.awery.util.NiceUtils;

public class AndroidImage extends Image {
	private final Drawable drawable;
	@DrawableRes
	private final int res;
	private final String name;

	public AndroidImage(Context context, @DrawableRes int res) {
		this.drawable = AppCompatResources.getDrawable(context, res);
		this.res = res;
		this.name = null;
	}

	public AndroidImage(@DrawableRes int res) {
		this(getAnyActivity(AppCompatActivity.class), res);
	}

	public AndroidImage(Context context, String name) {
		this.name = name;
		this.res = getResourceId(R.drawable.class, name);
		this.drawable = AppCompatResources.getDrawable(context, res);
	}

	public AndroidImage(String name) {
		this(getAnyActivity(AppCompatActivity.class), name);
	}

	public AndroidImage(Drawable drawable) {
		this.drawable = drawable;
		this.res = 0;
		this.name = null;
	}

	@DrawableRes
	public int getRes() {
		return res;
	}

	public String getRawRes() {
		return name;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public void applyTo(ImageView imageView) {
		if(drawable != null) {
			imageView.setImageDrawable(drawable);
		} else if(name != null) {
			if(NiceUtils.isUrlValid(name)) {
				Glide.with(imageView)
						.load(name)
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(imageView);
			} else {
				throw new UnsupportedOperationException("Cannot decide how to show an image!");
			}
		} else {
			throw new UnsupportedOperationException("Cannot decide how to show an image!");
		}
	}
}