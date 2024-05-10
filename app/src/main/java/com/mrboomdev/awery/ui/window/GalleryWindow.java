package com.mrboomdev.awery.ui.window;

import static com.mrboomdev.awery.app.AweryApp.addOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.drawableToBitmap;
import static com.mrboomdev.awery.app.AweryApp.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Fade;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Objects;

public class GalleryWindow extends PopupWindow {
	private final Context context;
	private final Runnable backPressCallback = () -> {
		if(isShowing()) {
			dismiss();
		}
	};
	private SubsamplingScaleImageView imageView;
	private LinearLayoutCompat linear;

	public GalleryWindow(Context context, Drawable... images) {
		super(MATCH_PARENT, MATCH_PARENT);
		this.context = context;

		init(images);
		createView();

		var image = drawableToBitmap(images[0]);
		imageView.setImage(ImageSource.cachedBitmap(image));
	}

	public GalleryWindow(Context context, String... images) {
		super(context);
		this.context = context;

		init(images);
		createView();

		Glide.with(context)
				.load(images[0])
				.into(new CustomTarget<Drawable>() {
					@Override
					public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
						imageView.setImage(ImageSource.cachedBitmap(drawableToBitmap(resource)));
					}

					@Override
					public void onLoadCleared(@Nullable Drawable placeholder) {
						imageView.recycle();
					}
				});
	}

	private void init(Object[] images) {
		if(images == null || images.length == 0) {
			throw new RuntimeException("No images provided");
		}

		if(images.length > 1) {
			throw new UnimplementedException("PLEASE TELL MRBOOMDEV THAT THIS THING HAS HAPPENED #GalleryWindowMultipleImages");
		}

		setClippingEnabled(true);
		setFocusable(true);
		setBackgroundDrawable(new ColorDrawable(0xAA000000));
		setEnterTransition(new Fade(Fade.IN));
		setExitTransition(new Fade(Fade.OUT));
	}

	public void show(@NonNull View rootView) {
		showAtLocation(rootView.getRootView(), Gravity.NO_GRAVITY, 0, 0);
		addOnBackPressedListener(Objects.requireNonNull(getActivity(context)), backPressCallback);
	}

	private void createView() {
		linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		setContentView(linear);

		imageView = new SubsamplingScaleImageView(context);
		imageView.setMaxScale(10);
		linear.addView(imageView, MATCH_PARENT, MATCH_PARENT);

		setOnDismissListener(() -> removeOnBackPressedListener(
				Objects.requireNonNull(getActivity(context)), backPressCallback));
	}
}